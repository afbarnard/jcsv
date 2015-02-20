Jubilo!  Cogitationis Summa Virtus
==================================


Hooray!  A CSV reader for Java that is fast, memory-efficient, and has a
nice API!

This project is my way of addressing the lack of a CSV parser in the
Java standard library and providing features not in the Apache Commons
CSV project.  (Apache CSV was not yet available when I started this code
or I might have settled on it.)  Plus, it is fun to tinker around with
designing and implementing efficient parsing.


License
-------

JCSV is free, open source software.  It is released under the MIT
license.  See the `LICENSE` file in your distribution for details.


Features
--------

* Fast (TODO benchmark evidence vs. Apache CSV)
* Memory-efficient: files of any size are processed with a fixed memory
  footprint (provided the user releases resources after use)
* Customizable to your dialect of delimited text
* Provenance
* Multiple access modes
  * Records individually or all at once
  * Errors as they occur or accumulated


Requirements
------------

* Java 7 (or later)


Contact
-------

After searching the existing documentation and issues, open an issue to
report a bug, make a feature request, ask a question, or to contact me
about anything else relating to this project.


Copyright (c) 2015 Aubrey Barnard.  This is free software.  See LICENSE
for details.
