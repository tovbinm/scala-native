package scala.scalanative
package build

import java.nio.file.{Files, Path, Paths}
import scala.sys.process.Process
import scalanative.build.IO.RichPath
import scalanative.nir.Global
import scalanative.linker.Link
import scalanative.sema.Sema
import scalanative.codegen.CodeGen
import scalanative.optimizer.Optimizer

/** Internal utilities to instrument Scala Native linker, otimizer and codegen. */
private[scalanative] object ScalaNative {

  /** Compute all globals that must be reachable
   *  based on given configuration.
   */
  def entries(config: Config): Seq[Global] = {
    val mainClass = Global.Top(config.mainClass)
    val entry =
      Global.Member(mainClass,
                    "main_scala.scalanative.runtime.ObjectArray_unit")
    entry +: CodeGen.depends
  }

  /** Given the classpath and main entry point, link under closed-world
   *  assumption.
   */
  def link(config: Config, entries: Seq[Global]): linker.Result = {
    config.logger.time("Linking") {
      Link(config, entries)
    }
  }

  /** Optimizer high-level NIR under closed-world assumption. */
  def optimize(config: Config,
               driver: optimizer.Driver,
               entries: Seq[nir.Global],
               assembly: Seq[nir.Defn]): Seq[nir.Defn] =
    config.logger.time(s"Optimizing (${config.mode} mode)") {
      Optimizer(config, driver, entries, assembly)
    }

  /** Given low-level assembly, emit LLVM IR for it to the buildDirectory. */
  def codegen(config: Config,
              entries: Seq[nir.Global],
              assembly: Seq[nir.Defn],
              dyns: Seq[String]): Seq[Path] = {
    config.logger.time("Generating intermediate code") {
      CodeGen(config, entries, assembly, dyns)
    }
    val produced = IO.getAll(config.workdir, "glob:**.ll")
    config.logger.info(s"Produced ${produced.length} files")
    produced
  }
}
