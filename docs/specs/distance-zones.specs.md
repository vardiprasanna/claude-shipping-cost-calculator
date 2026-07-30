# Distance Zones

**As an online retailer, I want shipping costs adjusted by destination zone, so that international deliveries reflect higher logistics costs.**

## Rules and Examples

### Rule: Must apply a zone multiplier to the weight-tier base rate to produce the zoned rate

Domestic ×1.0, European ×1.5, International ×2.5. The zoned rate is rounded to 2 decimal places (`HALF_UP`) immediately at this step, the same as the base rate — every breakdown field is an exact, independently-verifiable value.

| Base Rate | Zone | Multiplier | Zoned Rate |
|---|---|---|---|
| $8.99 | Domestic | ×1.0 | $8.99 |
| $8.99 | European | ×1.5 | $13.49 |
| $8.99 | International | ×2.5 | $22.48 |
| $2.99 | European | ×1.5 | $4.49 |

The last row is a boundary: $2.99 × 1.5 = $4.485 exactly, pinning down that the zoned rate rounds `HALF_UP` to $4.49 at this step, not $4.48.

---

### Rule: Must reject a shipment with an unrecognized destination zone as a 400 Bad Request

Zone matching is exact-spelling and case-sensitive — only `DOMESTIC`, `EUROPEAN`, `INTERNATIONAL` are valid. Any other value, including a case variant like `domestic`, is rejected.

- **Example:** The one where zone = "MARS" → 400 Bad Request, no cost calculated.
- **Counter-example:** The one where zone = "INTERNATIONAL" (the least common but still valid zone) → accepted, ×2.5 applied.

---

## Resolved decisions (for implementation)

- **Rounding point:** The zoned rate is rounded to 2dp/`HALF_UP` right after the multiplier is applied, not deferred to the final total.
- **Zone matching:** Case-sensitive, exact enum spelling only. Lowercase or mixed-case variants are rejected as unrecognized, same as any other invalid string.
- **Out of scope:** Missing/absent zone field (handled by general request validation, not this spec) — consistent with the precedent set in `weight-tiers.specs.md`.
