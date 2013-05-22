package org.twodee.speccheck.example.key;

import org.twodee.speccheck.Specified;

@Specified(inPackage = "org.twodee.speccheck.example", checkSuper = true)
public class InPackageTypeB extends org.twodee.speccheck.example.InPackageTypeA {
}
