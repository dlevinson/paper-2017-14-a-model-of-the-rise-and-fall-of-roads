# Finality Review Decision

- Row ID: `paper-2017-14`
- Title: A Model Of The Rise And Fall Of Roads
- Decision time: 2026-05-17 22:26:28 AEST
- Current routing: move to `UPLOADED`

The local ADAM4.1 source tree is a plausible candidate for the model used in the paper, and a filtered copy has been staged in this package. Direct paper comparison prevents treating it as ready: the paper reports 7,976 nodes and 20,914 links, while the staged ADAM4.1 Twin Cities inputs contain 7,393 node records and 20,380 link records.

This package should not remain in READY-TO-UPLOAD until one of these is resolved:

1. ADAM4.1 is confirmed as the final/best surviving paper package despite the count mismatch.
2. A final paper-specific ADAM/network package is located and substituted.
3. A public package is intentionally released as a legacy candidate with the mismatch documented.
