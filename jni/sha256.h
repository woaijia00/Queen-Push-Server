/*
 * sha256.h
 *
 *  Created on: 2012-8-30
 *      Author: Zhangzhuo
 */
#include <stdio.h>
#include <string.h>
#include "environment.h"

#if BYTE_ORDER == LITTLE_ENDIAN
  #define SWAP(n) \
    (((n) << 24) | (((n) & 0xff00) << 8) | (((n) >> 8) & 0xff00) | ((n) >> 24))
#else
  #define SWAP(n) (n)
#endif

#ifndef SHA256_H_
  #define SHA256_H_

    typedef struct sha256_ctx {
        word32 state[8];
        word32  total[2];
        word32  buflen;
        byte    buffer[128] __attribute__((__aligned__(__alignof__(word32))));
    } sha256_ctx;

    void sha256_initial(sha256_ctx *ctx);

    void sha256_final(sha256_ctx *ctx, word32 resbuf[8]);

    void sha256_process(sha256_ctx *ctx, const void *data, size_t len);
#endif /* SHA256_H_ */
