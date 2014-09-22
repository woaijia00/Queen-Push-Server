/*
 * Elliptic curve cryptographic primitives
 *
 * This public domain software was written by Paulo S.L.M. Barreto
 * <pbarreto@uninet.com.br> based on original C++ software written by
 * George Barwood <george.barwood@dial.pipex.com>
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHORS ''AS IS'' AND ANY EXPRESS
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE AUTHORS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

#include "ec_curve.h"
#include "ec_vlong.h"
#include "ec_crypt.h"

void cpMakePublicKey(vlPoint vlPublicKey, const vlPoint vlPrivateKey, lunit *expt, lunit *logt)
{
	ecPoint ecPublicKey;

	ecCopy(&ecPublicKey, &curve_point);
	ecMultiply(&ecPublicKey, vlPrivateKey, expt, logt);
	ecPack(&ecPublicKey, vlPublicKey, expt, logt);
} /* cpMakePublicKey */

void cpEncodeSecret(const vlPoint vlPublicKey, vlPoint vlMessage, vlPoint vlSecret, lunit *expt, lunit *logt)
{
	ecPoint q;

	ecCopy(&q, &curve_point);
	ecMultiply(&q, vlSecret, expt, logt);
	ecPack(&q, vlMessage, expt, logt);
	ecUnpack(&q, vlPublicKey, expt, logt);
	ecMultiply(&q, vlSecret, expt, logt);
	gfPack(q.x, vlSecret);
} /* cpMakeSecret */

void cpDecodeSecret(const vlPoint vlPrivateKey, const vlPoint vlMessage, vlPoint d, lunit *expt, lunit *logt)
{
	ecPoint q;

	ecUnpack(&q, vlMessage, expt, logt);
	ecMultiply(&q, vlPrivateKey, expt, logt);
	gfPack(q.x, d);
} /* ecDecodeSecret */

void cpSign(const vlPoint vlPrivateKey, const vlPoint k, const vlPoint vlMac, cpPair * sig, lunit *expt, lunit *logt)
{
	ecPoint q;
	vlPoint tmp;

	ecCopy(&q, &curve_point);
	ecMultiply(&q, k, expt, logt);
	gfPack(q.x, sig->r);
	vlAdd(sig->r, vlMac);
	vlRemainder(sig->r, prime_order);
	if (sig->r[0] == 0) return;
	vlMulMod(tmp, vlPrivateKey, sig->r, prime_order);
	vlCopy(sig->s, k);
	if (vlGreater(tmp, sig->s)) vlAdd(sig->s, prime_order);
	vlSubtract(sig->s, tmp);
} /* cpSign */

int cpVerify(const vlPoint vlPublicKey, const vlPoint vlMac, cpPair * sig, lunit *expt, lunit *logt)
{
	ecPoint t1, t2;
	vlPoint t3, t4;

	ecCopy(&t1, &curve_point);
	ecMultiply(&t1, sig->s, expt, logt);
	ecUnpack(&t2, vlPublicKey, expt, logt);
	ecMultiply(&t2, sig->r, expt, logt);
	ecAdd(&t1, &t2, expt, logt);
	gfPack(t1.x, t4);
	vlRemainder(t4, prime_order);
	vlCopy(t3, sig->r);
	if (vlGreater(t4, t3)) vlAdd(t3, prime_order);
	vlSubtract(t3, t4);
	return vlEqual(t3, vlMac);
} /* cpVerify */
