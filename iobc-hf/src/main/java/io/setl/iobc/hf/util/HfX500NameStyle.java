package io.setl.iobc.hf.util;

import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;

/**
 * Outputs X500 names in the style used by Fabric.
 *
 * @author Simon Greatrix on 04/02/2022.
 */
public class HfX500NameStyle extends BCStyle {

  public static final HfX500NameStyle INSTANCE = new HfX500NameStyle();


  public String toString(X500Name name) {
    StringBuilder builder = new StringBuilder();
    StringBuffer buffer = new StringBuffer();

    RDN[] rdns = name.getRDNs();

    for (int i = 0; i < rdns.length; i++) {
      builder.append('/');

      IETFUtils.appendRDN(buffer, rdns[i], defaultSymbols);
      String value = buffer.toString().replace("/", "\\/");
      builder.append(value);
      buffer.setLength(0);
    }

    return builder.toString();
  }

}
