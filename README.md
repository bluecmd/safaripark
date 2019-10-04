# SaFariPark

This package contains an interface to interact with SFP+ modles. It is written in Java.
It is a fork of [sfp-plus-i2c](https://www.ohwr.org/project/sfp-plus-i2c) to add support for [fejkon](https://github.com/bluecmd/fejkon).

Original logo:

![Logo](https://raw.githubusercontent.com/bluecmd/safaripark/master/safaripark/resources/splash.png)

# Build

```
ant
```

# Run

```
java -jar safaripark/dist/safaripark.jar
```

# SFP providers

To add/remove supported SFP providers, modify `jsfp/src/META-INF/services/nl.nikhef.sfp.SFPProvider`.
