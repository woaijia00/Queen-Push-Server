package com.tgx.queen.base.disruptor.handler.cm;

import java.util.Arrays;
import java.util.Random;

import com.lmax.disruptor.EventHandler;
import com.tgx.queen.base.disruptor.bean.Event;
import com.tgx.queen.base.disruptor.bean.TgxEvent;
import com.tgx.queen.base.disruptor.handler.inf.EventOp;
import com.tgx.queen.base.util.CryptUtil;
import com.tgx.queen.base.util.IoUtil;
import com.tgx.queen.io.bean.protocol.impl.X01_AsymmetricPub;
import com.tgx.queen.socket.aio.impl.AioContext;


public class EncryptHandler
        implements
        EventHandler<TgxEvent>

{
	@Override
	public void onEvent(TgxEvent event, long sequence, boolean endOfBatch) throws Exception {
		EventOp op = event.getOperator();
		if (op != null) switch (op.getSerialNum()) {
			case EventOp.TGX_CHANNEL_SERIAL://TgxOpChannelHandle
			case EventOp.TGX_CM_CLIENT_SERIAL://TgxOpClientMesssage
			case EventOp.TGX_CM_REGISTER_SERIAL://TgxOpCmRegister
			case EventOp.TGX_ENCRYPT_HS_SERIAL://TgxOpEncryptHandShake
				event.forkHandler = this;
				EventOp rOp = op.hasError() ? op.errOp(event) : op.op(event);
				event.eventOp(rOp, event.getErrorType());
				if (rOp == null) event.reset();
				break;
		}
		else System.err.println("EncryptHandler NILL  Operator!");
	}
	
	private final int                 _index;
	private final static int          ECCPAIR_INDEX_PUBKEY  = 0;
	private final static int          ECCPAIR_INDEX_PASSWD  = ECCPAIR_INDEX_PUBKEY + 1;
	private final static int          ECCPAIR_INDEX_TIME    = ECCPAIR_INDEX_PASSWD + 1;
	private final static int          ECCPAIR_INDEX_VERSION = ECCPAIR_INDEX_TIME + 1;
	private final static int          ECC_KEY_TIME_MAX      = 1 << 17;
	private final static int          total_size_width      = 8;
	private final static int          index_width           = 1;
	private final static int          per_size_width        = total_size_width - index_width;
	private final static int          pair_size             = 1 << per_size_width;
	private final static int          pair_size_mask        = pair_size - 1;
	private final static int          index_mask            = ((1 << index_width) - 1) << per_size_width;
	private final static int          version_width         = 12;                                                             //<16
	private final static int          version_mask          = ((1 << version_width) - 1) << total_size_width;
	private final static int          salt_mask             = (0xFFFFFFFF << (total_size_width + version_width)) ^ 0x80000000;
	public final static int           encryptCount          = 1 << index_width;
	private final static byte[][][][] eccPairs              = new byte[encryptCount][pair_size][][];
	public final Random               random;
	public final CryptUtil            crypt;
	private int                       tmp_pubKey_id         = -1;
	private int                       indexAdd;
	
	public EncryptHandler(final int index, final boolean _server_Initialize) {
		_index = index;
		random = new Random();
		crypt = new CryptUtil();
		if (_server_Initialize)
		{
			System.out.println(getClass().getSimpleName() + " Init Start");
			long t = System.currentTimeMillis();
			for (int i = 0, j = _index << per_size_width; i < pair_size; i++)
				createPair(random, i | j);
			long te = (System.currentTimeMillis() - t) / pair_size;
			System.out.println("per get pubKey : " + te);
		}
	}
	
	public final static int getForkIndex(int cmd_pubKey_id) {
		return (cmd_pubKey_id & index_mask) >>> per_size_width;
	}
	
	public final static boolean isPubKeyAvailable(int cmd_pubKey_id) {
		return cmd_pubKey_id >= 0;
	}
	
	public EncryptHandler(final int index) {
		this(index, false);
	}
	
	public void resX01(final int cmd_pubKey_id, Event event) {
		AioContext context = event.session.getContext();
		X01_AsymmetricPub x01 = new X01_AsymmetricPub();
		x01.pubKey = createPair(random, cmd_pubKey_id)[ECCPAIR_INDEX_PUBKEY];
		context.pubKey_id = x01.pubKey_id = tmp_pubKey_id;
		event.attach = x01;
	}
	
	public byte[] getRc4Key(final int cmd_pubKey_id, byte[] encryption) {
		if (cmd_pubKey_id < 0) return null;
		int forkIndex = (cmd_pubKey_id & index_mask) >>> per_size_width;
		int keyIndex = cmd_pubKey_id & pair_size_mask;
		int keyVersion = (cmd_pubKey_id & version_mask) >>> total_size_width;
		byte[][] eccPair = null;
		if (isPubKeyAvailable(cmd_pubKey_id) && forkIndex == _index) eccPair = eccPairs[forkIndex][keyIndex];
		if (eccPair != null && IoUtil.readUnsignedShort(eccPair[ECCPAIR_INDEX_VERSION], 0) == keyVersion)
		{
			String password = new String(eccPair[ECCPAIR_INDEX_PASSWD]);
			if (password.equals("")) return null;
			return crypt._eccDecrypt(new String(eccPair[ECCPAIR_INDEX_PASSWD]), encryption);
		}
		return null;
	}
	
	private final byte[][] createPair(Random random, int cmd_pubKey_id) {
		int forkIndex = (cmd_pubKey_id & index_mask) >>> per_size_width;
		int keyIndex = cmd_pubKey_id & pair_size_mask;
		int keyVersion = (cmd_pubKey_id & version_mask) >>> total_size_width;
		int mix_pubkey_id = (forkIndex << per_size_width) | keyIndex | (keyVersion << total_size_width);
		int sequence;
		byte[][] eccPair = null;
		if (isPubKeyAvailable(cmd_pubKey_id) && forkIndex == _index) eccPair = eccPairs[forkIndex][keyIndex];
		if (eccPair != null)
		{
			tmp_pubKey_id = mix_pubkey_id;
			sequence = IoUtil.readInt(eccPair[ECCPAIR_INDEX_TIME], 0) + 1;
			if (sequence < ECC_KEY_TIME_MAX) IoUtil.writeInt(sequence, eccPair[ECCPAIR_INDEX_TIME], 0);
			else
			{
				String passwd = "tgx-" + Long.toOctalString(random.nextLong()) + "pass";
				eccPair[ECCPAIR_INDEX_PUBKEY] = crypt._pubKey(passwd);
				eccPair[ECCPAIR_INDEX_PASSWD] = passwd.getBytes();
				int version = (1 + IoUtil.readUnsignedShort(eccPair[ECCPAIR_INDEX_VERSION], 0)) & (version_mask >>> total_size_width);
				IoUtil.writeShort(version, eccPair[ECCPAIR_INDEX_VERSION], 0);
				IoUtil.writeInt(0, eccPair[ECCPAIR_INDEX_TIME], 0);
				tmp_pubKey_id |= version << total_size_width;
				System.out.println("Encrypt old: " + cmd_pubKey_id + " new: " + tmp_pubKey_id);
			}
			return eccPair;
		}
		int pairIndex = indexAdd & pair_size_mask;
		eccPair = eccPairs[_index][pairIndex];
		tmp_pubKey_id = indexAdd++ & salt_mask | (_index << per_size_width) | pairIndex;
		if (eccPair == null)
		{
			String passwd = "tgx-" + Long.toOctalString(random.nextLong()) + "pass";
			byte[] time = new byte[4];
			byte[] version = new byte[4];
			Arrays.fill(time, (byte) 0);
			Arrays.fill(version, (byte) 0);
			eccPair = new byte[][] {
			        crypt._pubKey(passwd),
			        passwd.getBytes(),
			        time,
			        version
			};
			eccPairs[_index][pairIndex] = eccPair;
		}
		return eccPair;
	}
}
