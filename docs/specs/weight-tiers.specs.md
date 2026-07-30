# Weight Tiers

**As an online retailer, I want shipping costs calculated based on parcel weight using tiered pricing, so that heavier parcels are charged appropriately.**

## Rules and Examples

### Rule: Must apply a flat base rate according to which weight tier a parcel falls into

| Weight | Tier | Base Rate |
|---|---|---|
| 0.999 kg | under 1kg | $2.99 |
| 1.000 kg | 1kg–20kg | $8.99 |
| 20.000 kg | 1kg–20kg | $8.99 |

The 1kg–20kg tier is inclusive at both ends: 1.000kg is the lower boundary (belongs to this tier, not "under 1kg"), and 20.000kg is the upper boundary (belongs to this tier, not the over-20kg surcharge below).

---

### Rule: Must add a $0.50/kg surcharge on top of the $8.99 base rate for the portion of weight strictly over 20kg

- **Example:** The one where a parcel weighs 25kg → $8.99 + ($0.50 × 5kg) = $11.49.
- **Counter-example:** The one where a parcel weighs exactly 20.000kg → no surcharge applies (it's the base tier's upper boundary, not "over 20kg"), total is $8.99.

---

### Rule: Must reject a shipment with zero or negative weight as a 400 Bad Request

- **Example:** The one where weight is -2kg → 400 Bad Request, no cost calculated.
- **Counter-example:** The one where weight is 0.001kg (smallest valid positive weight) → accepted, priced at the under-1kg tier, $2.99.

---

## Resolved decisions (for implementation)

- **5kg–20kg rate:** Confirmed as the same $8.99 flat rate as the 1kg tier — there is no separate 5kg tier; "1-5 kg" in the original story was effectively shorthand and the real tier boundary is 20kg.
- **20kg boundary:** Belongs to the base $8.99 tier (inclusive), not the surcharge tier, consistent with the 1kg lower-bound-inclusive rule.
- **Out of scope:** Missing/non-numeric weight values (handled by general request validation, not this spec). No upper cap on weight — the over-20kg formula is unbounded.
