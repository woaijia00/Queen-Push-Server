#ifndef __EC_CURVE_H
#define __EC_CURVE_H

#include <stddef.h>

#include "environment.h"
#include "ec_field.h"
#include "ec_vlong.h"

typedef struct
{
	gfPoint x, y;
} ecPoint;

extern const vlPoint prime_order;
extern const ecPoint curve_point;

#ifdef SELF_TESTING

int ecCheck (const ecPoint *p,lunit *expt,lunit *logt);
/* confirm that y^2 + x*y = x^3 + EC_B for point p */

void ecPrint (FILE *out, const char *tag, const ecPoint *p);
/* printf prefix tag and the contents of p to file out */

int ecEqual (const ecPoint *p, const ecPoint *q);
/* evaluates to 1 if p == q, otherwise 0 (or an error code) */
#endif /* SELF_TESTING */
void ecCopy(ecPoint *p, const ecPoint *q);
/* sets p := q */

int ecCalcY(ecPoint *p, int ybit, lunit *expt, lunit *logt);
/* given the x coordinate of p, evaluate y such that y^2 + x*y = x^3 + EC_B */
#ifdef SELF_TESTING
void ecRandom (ecPoint *p,lunit *expt,lunit *logt);
/* sets p to a random point of the elliptic curve defined by y^2 + x*y = x^3 + EC_B */

void ecClear (ecPoint *p);
/* sets p to the point at infinity O, clearing entirely the content of p */
#endif /* SELF_TESTING */
void ecAdd(ecPoint *p, const ecPoint *r, lunit *expt, lunit *logt);
/* sets p := p + r */

void ecSub(ecPoint *p, const ecPoint *r, lunit *expt, lunit *logt);
/* sets p := p - r */
#ifdef SELF_TESTING
void ecNegate (ecPoint *p);
/* sets p := -p */
#endif
void ecDouble(ecPoint *p, lunit *expt, lunit *logt);
/* sets p := 2*p */

void ecMultiply(ecPoint *p, const vlPoint k, lunit *expt, lunit *logt);
/* sets p := k*p */

int ecYbit(const ecPoint *p, lunit *expt, lunit *logt);
/* evaluates to 0 if p->x == 0, otherwise to gfYbit (p->y / p->x) */

void ecPack(const ecPoint *p, vlPoint k, lunit *expt, lunit *logt);
/* packs a curve point into a vlPoint */

void ecUnpack(ecPoint *p, const vlPoint k, lunit *expt, lunit *logt);
/* unpacks a vlPoint into a curve point */
#ifdef SELF_TESTING
int ecSelfTest (int test_count);
/* perform test_count self tests */
#endif /* SELF_TESTING */
#endif /* __EC_CURVE_H */
