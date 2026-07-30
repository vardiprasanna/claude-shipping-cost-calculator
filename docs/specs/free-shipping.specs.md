# Free Shipping

**As an online retailer, I want to offer free shipping on qualifying domestic orders, so that I can incentivise larger purchases.**

## Rules and Examples

### Rule: Must waive the shipping cost entirely for a domestic order whose total is $75.00 or more

| Zone | Order Total | Shipping Cost |
|---|---|---|
| Domestic | $74.99 | $8.99 |
| Domestic | $75.00 | $0.00 |
| Domestic | $100.00 | $0.00 |
| European | $100.00 | $13.49 |
| International | $100.00 | $22.48 |

The $74.99/$75.00 pair pins the boundary as inclusive (`>=`, not `>`). The European and International rows carry the same $100.00 order total as the qualifying Domestic row, showing the waiver never applies outside the Domestic zone, regardless of total.

---

### Rule: Must apply the free-shipping waiver after weight and zone charges are calculated, overriding even a heavy, surcharged parcel's cost

- **Example:** The one where a 25kg domestic parcel with a $100.00 order total → the pre-waiver cost would be $11.49 (weight surcharge + zone), but the waiver reduces `totalCost` to $0.00.
- **Counter-example:** The one where the same 25kg domestic parcel has only a $50.00 order total (below the threshold) → the full $11.49 is charged, no waiver.

---

## Resolved decisions (for implementation)

- **Breakdown shape:** `breakdown.zonedRate` keeps showing the pre-waiver computed rate; a separate `breakdown.freeShippingApplied` boolean is set to `true` when the waiver fires. `totalCost` becomes `$0.00` only when the flag is `true`. Every breakdown field stays independently verifiable, consistent with the precedent set in `distance-zones.specs.md`.
- **`orderTotal` field:** already exists on `ShippingRequest` (added during Distance Zones) — this feature is a new consumer of it, not a model change.
- **Out of scope:** missing/absent `orderTotal` (handled by general request validation, not this spec) — consistent with the precedent set in `weight-tiers.specs.md` and `distance-zones.specs.md`.
