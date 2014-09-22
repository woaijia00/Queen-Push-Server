/*
 * environment.h
 *
 *  Created on: 2012-7-23
 *      Author: zhangzhuo
 */

#ifndef ENVIRONMENT_H_
#define ENVIRONMENT_H_
    typedef unsigned char           byte;  /*  8 bit */
    typedef unsigned short          word16; /* 16 bit */
    typedef unsigned char           boolean;
#define true    1
#define false   0
#ifdef _LP64
  #include <machine/endian.h>
        typedef int                 xword32;
        typedef long                xword64;
        typedef unsigned int        word32; /* 32 bit */
        typedef unsigned long       word64; /* 64 bit */
#else
  #include <endian.h>
        typedef long                xword32;
        typedef long long           xword64;
        typedef unsigned long       word32; /* 32 bit */
        typedef unsigned long long  word64; /* 64 bit */
#endif
#endif /* ENVIRONMENT_H_ */
