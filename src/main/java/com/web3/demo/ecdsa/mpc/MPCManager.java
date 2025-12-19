package com.web3.demo.ecdsa.mpc;

import java.util.List;
import org.bouncycastle.math.ec.ECPoint;

public class MPCManager {

    public static ECPoint aggregatePublicKeys(List<ECPoint> publicShares) {
        ECPoint aggregated = null;

        for (ECPoint p : publicShares) {
            if (aggregated == null) {
                aggregated = p;
            } else {
                aggregated = aggregated.add(p);
            }
        }
        return aggregated.normalize();
    }
}