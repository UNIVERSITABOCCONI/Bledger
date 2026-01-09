package it.bocconi.bledger.feature.smartcontract.utils;

import it.bocconi.bledger.feature.network.router.dto.NetworkTreeDto;
import org.web3j.crypto.Hash;
import org.web3j.utils.Numeric;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TreeHasher {

    private TreeHasher(){

    }

    public static String hashNodeUnordered(NetworkTreeDto n) {
        List<NetworkTreeDto> kids = n.getChildren() == null ? List.of() : n.getChildren();

        List<String> childHashes = new ArrayList<>(kids.size());
        for (NetworkTreeDto c : kids) {
            childHashes.add(hashNodeUnordered(c));
        }

        childHashes.sort(Comparator.naturalOrder());

        byte[] payload = serializeCompanyId(n.getCompanyId(), childHashes);

        return Numeric.toHexStringNoPrefix(Hash.sha3(payload));
    }

    private static byte[] serializeCompanyId(String companyId, List<String> childHashes) {
        byte[] companyIdBytes = str(companyId).getBytes(StandardCharsets.UTF_8);

        int size = 4 + companyIdBytes.length + 4 + childHashes.size() * 32;

        ByteBuffer buf = ByteBuffer.allocate(size);

        buf.putInt(companyIdBytes.length);
        buf.put(companyIdBytes);

        buf.putInt(childHashes.size());

        for (String h : childHashes) {
            byte[] raw = Numeric.hexStringToByteArray(h);
            buf.put(raw);
        }

        buf.flip();
        byte[] out = new byte[buf.remaining()];
        buf.get(out);
        return out;
    }

    private static String str(String s) {
        return s == null ? "" : s;
    }
}
