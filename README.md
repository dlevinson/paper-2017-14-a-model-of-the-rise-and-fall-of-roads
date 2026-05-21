# A Model Of The Rise And Fall Of Roads

## Bibliographic information

- Row ID: `paper-2017-14`
- Citation: Zhang, L., & Levinson, D. (2017). A Model Of The Rise And Fall Of Roads. *Journal of Transport and Land Use*, 10(1), 337-356. https://doi.org/10.5198/jtlu.2016.887
- Public paper repositories: https://hdl.handle.net/11299/187851 and https://hdl.handle.net/11299/180072
- Last package cleanup: 2026-05-17 22:26:28 AEST

## Current status

This is not ready to upload as a final public reproducibility package. The local ADAM4.1 folder is a plausible model-code candidate and has now been staged, but the staged Twin Cities network files do not match the exact network size reported in the paper.

The paper reports a 2000 Twin Cities network with 7,976 nodes and 20,914 links. The staged ADAM4.1 candidate files contain 7,393 node records and 20,380 link records. That mismatch may reflect a different model version, a filtered network, or a missing final network package.

## What this package contains

- `paper/`: paper PDF.
- `code/adam4_1_candidate/`: filtered Java source from the local ADAM4.1 candidate.
- `data/twin_cities_adam4_1_candidate/`: candidate Twin Cities network inputs bundled with ADAM4.1.
- `data/examples/`: small Sioux Falls example inputs bundled with ADAM4.1.
- `documentation/adam4_1_candidate/`: legacy model documentation copied from ADAM4.1.
- `metadata/`: current manifests and package-boundary decisions.

## Finality question

Confirm whether ADAM4.1 is the final code package used for the JTLU paper and whether another final Twin Cities network bundle exists with the paper-reported 7,976 nodes and 20,914 links. If ADAM4.1 is accepted as the best surviving package despite the mismatch, this can move back to public ready-to-upload with that caveat documented.

<!-- package-hardening-status:start -->
## Package Hardening Status

Generated: 2026-05-22 06:51:10 AEST

- Pipeline: `READY-TO-UPLOAD/PUBLIC`
- Sidecars added/updated: `PACKAGE_STATUS.md`, `PACKAGE_MANIFEST.csv`, `LICENSE_STATUS.md`.
- Paper reference copies are for local audit convenience and are not public-upload assets without rights review.
- Final GitHub upload should use the manifest include statuses and the license-status note.
<!-- package-hardening-status:end -->
