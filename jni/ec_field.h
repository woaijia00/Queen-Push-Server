#ifndef __EC_FIELD_H
#define __EC_FIELD_H

#include <stdio.h>

#include "environment.h"
#include "ec_param.h"
#include "ec_vlong.h"

#define GF_POINT_UNITS	(2*(GF_K+1))

#if GF_L < 8 || GF_L > 16
#error "this implementation assumes 8 <= GF_L <= 16"
#endif

#if GF_L ==  8
#define BITS_PER_LUNIT 8
typedef byte lunit;
#else
#define BITS_PER_LUNIT 16
typedef word16 lunit;
#endif

#if GF_L == 16
typedef word32 ltemp;
#else
typedef word16 ltemp;
#endif

typedef lunit gfPoint[GF_POINT_UNITS];

/* interface functions: */

int gfInit(lunit **expt, lunit **logt);
/* initialize the library ---> MUST be called before any other gf-function */

void gfQuit(lunit *expt, lunit *logt);
/* perform housekeeping for library termination */
#ifdef SELF_TESTING
void gfPrint (FILE *out, const char *tag, const gfPoint p);
/* printf prefix tag and the contents of p to file out */
#endif

int gfEqual(const gfPoint p, const gfPoint q);
/* evaluates to 1 if p == q, otherwise 0 (or an error code) */
void gfClear(gfPoint p);
/* sets p := 0, clearing entirely the content of p */

#ifdef SELF_TESTING
void gfRandom (gfPoint p);
/* sets p := <random field element> */
#endif
void gfCopy(gfPoint p, const gfPoint q);
/* sets p := q */

void gfAdd(gfPoint p, const gfPoint q, const gfPoint r, lunit *expt, lunit *logt);
/* sets p := q + r */

void gfMultiply(gfPoint r, const gfPoint p, const gfPoint q, lunit *expt, lunit *logt);
/* sets r := p * q mod (x^GF_K + x^GF_T + 1) */

void gfSmallDiv(gfPoint p, lunit b, lunit *expt, lunit *logt);
/* sets p := (b^(-1))*p mod (x^GF_K + x^GF_T + 1) for b != 0 (of course...) */

void gfSquare(gfPoint p, const gfPoint q, lunit *expt, lunit *logt);
/* sets p := q^2 mod (x^GF_K + x^GF_T + 1) */

int gfInvert(gfPoint p, const gfPoint q, lunit *expt, lunit *logt);
/* sets p := q^(-1) mod (x^GF_K + x^GF_T + 1) */
/* warning: p and q must not overlap! */

void gfSquareRoot(gfPoint p, lunit b, lunit *expt, lunit *logt);
/* sets p := sqrt(b) = b^(2^(GF_M-1)) */

int gfTrace(const gfPoint p, lunit *expt, lunit *logt);
/* quickly evaluates to the trace of p (or an error code) */

int gfQuadSolve(gfPoint p, const gfPoint q, lunit *expt, lunit *logt);
/* sets p to a solution of p^2 + p = q */

int gfYbit(const gfPoint p);
/* evaluates to the rightmost (least significant) bit of p (or an error code) */

void gfPack(const gfPoint p, vlPoint k);
/* packs a field point into a vlPoint */

void gfUnpack(gfPoint p, const vlPoint k);
/* unpacks a vlPoint into a field point */
#ifdef SELF_TESTING
int gfSelfTest (int test_count,lunit *expt,lunit *logt);
/* perform test_count self tests */
#endif
#endif /* __EC_FIELD_H */

