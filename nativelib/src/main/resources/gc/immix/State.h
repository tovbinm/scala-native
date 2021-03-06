#ifndef IMMIX_STATE_H
#define IMMIX_STATE_H

#include "Heap.h"

extern Heap heap;
extern Stack stack;
extern Allocator allocator;
extern LargeAllocator largeAllocator;

extern bool overflow;
extern word_t *currentOverflowAddress;

#endif // IMMIX_STATE_H
