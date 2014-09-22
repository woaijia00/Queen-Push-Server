#ifndef __EC_CRYPT_H
#define __EC_CRYPT_H

#include "ec_curve.h"
#include "ec_vlong.h"

typedef struct
{
	vlPoint r, s;
} cpPair;

void cpMakePublicKey(vlPoint vlPublicKey, const vlPoint vlPrivateKey, lunit *expt, lunit *logt);
void cpEncodeSecret(const vlPoint vlPublicKey, vlPoint vlMessage, vlPoint vlSecret, lunit *expt, lunit *logt);
void cpDecodeSecret(const vlPoint vlPrivateKey, const vlPoint vlMessage, vlPoint d, lunit *expt, lunit *logt);
void cpSign(const vlPoint vlPrivateKey, const vlPoint secret, const vlPoint mac, cpPair * cpSig, lunit *expt, lunit *logt);
int cpVerify(const vlPoint vlPublicKey, const vlPoint vlMac, cpPair * cpSig, lunit *expt, lunit *logt);

#endif /* __EC_CRYPT_H */
