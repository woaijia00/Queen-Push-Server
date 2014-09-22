#ifndef _SHA1_H
#define _SHA1_H

#include <string.h>
#include <stdio.h>

#include "environment.h"
#define HW 5

    typedef struct {
        word32  state[5];
        word32  count[2];
        byte    buffer[64];
    } sha1_ctx;

    void sha1_initial(sha1_ctx *c);

    void sha1_process(sha1_ctx *c, const void *data, size_t len);

    void sha1_final(sha1_ctx *c, word32[HW]);
#endif // END _SHA1_H
