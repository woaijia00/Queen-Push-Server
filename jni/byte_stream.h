/*
 *  byte_stream.h
 *
 *  Created on: 2012-8-3
 *      Author: Zhangzhuo
 */

#ifndef BYTE_STREAM_H_
#define BYTE_STREAM_H_

#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <assert.h>
#include "environment.h"

typedef word32 pos;
typedef struct ByteBuffer
{
	size_t capacity;
	word32 index;
	word32 size;
	pos mark;
	struct ByteBuffer* next;
	byte* data;
	void (*dispose)(struct ByteBuffer* self);
} ByteBuffer;

typedef struct ByteArrayStream
{
	struct ByteBuffer* first;
	struct ByteBuffer* writeAt;
	struct ByteBuffer* readAt;

	size_t (*write)(struct ByteArrayStream* out, byte value);
	size_t (*writeShort)(struct ByteArrayStream* out, word16 value);
	size_t (*writeInt)(struct ByteArrayStream* out, word32 value);
	size_t (*writeLong)(struct ByteArrayStream* out, word64 value);
	size_t (*writeArray)(struct ByteArrayStream* out, byte* src, size_t len);
	int (*read)(struct ByteArrayStream* in);
	size_t (*readArray)(struct ByteArrayStream* in, byte* dest, size_t len);
	byte* (*toArray)(struct ByteArrayStream* bas);
	void (*dispose)(struct ByteArrayStream* self);
} ByteArrayStream;

void byteStreamInit(ByteArrayStream* bas, size_t bufSize);

#endif /* BYTE_STREAM_H_ */
