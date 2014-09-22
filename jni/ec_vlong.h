#ifndef __EC_VLONG_H
#define __EC_VLONG_H

#include <stdio.h>
#include "environment.h"
#include "ec_param.h"


#define VL_UNITS ((GF_K*GF_L + 15)/16 + 1) /* must be large enough to hold a (packed) curve point (plus one element: the length) */
#define VL_SIZE (VL_UNITS + 2)

typedef word16 vlPoint[VL_SIZE];

#ifdef SELF_TESTING
void vlPrint (FILE *out, const char *tag, const vlPoint k);
/* printf prefix tag and the contents of k to file out */
#endif
void vlClear(vlPoint p);

void vlShortSet(vlPoint p, word16 u);
/* sets p := u */

int vlEqual(const vlPoint p, const vlPoint q);

int vlGreater(const vlPoint p, const vlPoint q);

int vlNumBits(const vlPoint k);
/* evaluates to the number of bits of k (index of most significant bit, plus one) */

int vlTakeBit(const vlPoint k, word16 i);
/* evaluates to the i-th bit of k */
#ifdef SELF_TESTING
void vlRandom (vlPoint k);
/* sets k := <random very long integer value> */
#endif
void vlCopy(vlPoint p, const vlPoint q);
/* sets p := q */

void vlAdd(vlPoint u, const vlPoint v);

void vlSubtract(vlPoint u, const vlPoint v);

void vlRemainder(vlPoint u, const vlPoint v);

void vlMulMod(vlPoint u, const vlPoint v, const vlPoint w, const vlPoint m);

void vlShortLshift(vlPoint u, byte n);

void vlShortRshift(vlPoint u, byte n);

int vlShortMultiply(vlPoint p, const vlPoint q, word16 d);
/* sets p = q * d, where d is a single digit */
#ifdef SELF_TESTING
int vlSelfTest (int test_count);
#endif
#endif /* __EC_VLONG_H */
