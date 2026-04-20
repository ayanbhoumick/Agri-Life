# Design Review Prompts

> **Auto-apply rule:** Whenever creating or modifying any file inside `src/main/resources/static/`
> (HTML, CSS, JS), run all 5 audits below before finalizing output. Do not skip to shipping —
> audit first, fix inline, then deliver.
>
> Run them in order: 01 → 02 → 03 → 04 → 05.
> Prompt 05 ("Why Does This Look Cheap?") is always the final pass.

---

## 01 — The Visual Hierarchy Surgeon

*Forces a surgical breakdown of attention flow so you control exactly where the eye goes and when.*

Your job is to act as a visual hierarchy surgeon, not a compliment machine.

Do this in order:
1. Tell me where the eye lands first, second, and third based purely on size, contrast, color weight, and position.
2. Tell me where the eye SHOULD land first, second, and third based on the business or communication goal.
3. Identify every element that's competing for attention it hasn't earned.
4. For each problem, give me one specific fix: exact font size change, contrast adjustment, spacing tweak, or removal.

**Rules:**
- No vague feedback like "improve the hierarchy." Name the element, name the fix.
- If something needs to be removed entirely, say so.
- Rank your fixes by impact. What one change would do the most work?

---

## 02 — The Typography Interrogation

*Stress tests every typographic decision and gives you a concrete upgrade path to close the $500 vs $5,000 gap.*

Audit the typography in this design like a senior type director. Go through each checkpoint and give me a verdict and fix for each:

**PAIRING**
Do the fonts create tension or harmony? Is that the right call for this context?
Are the fonts doing distinct jobs (display vs body vs UI) or are they stepping on each other?

**SCALE**
Is there enough size contrast between heading levels? (A common mistake: h1 and h2 are too close in size.)
Does the smallest text stay readable at actual viewing distance?

**SPACING**
Is line-height set for readability or left at default?
Is letter-spacing on headlines tightened?
Are paragraph widths staying within the 60–75 character ideal?

**WEIGHT AND HIERARCHY SIGNAL**
Is font weight doing contrast work, or just decorative?
Can someone tell primary, secondary, and tertiary text apart at a glance?

For each problem, give the specific value change.

---

## 03 — The Whitespace Pressure Test

*Premium design breathes. Amateur design suffocates. Find every place your layout is too tight.*

I want you to pressure test the whitespace and spacing in this design. Work through these questions:

**MACRO SPACING (sections, containers)**
Are the section gaps large enough to signal a new zone, or do sections bleed together?
Is there a consistent spatial rhythm (e.g., an 8pt grid) or does spacing feel ad hoc?

**MICRO SPACING (components, text, icons)**
Inside cards and components, is padding equal on all sides or does it look squeezed?
Do icons have enough clearance from adjacent text?
Are button labels getting enough horizontal padding?

**BREATHING ROOM**
Which elements need more isolation to feel important?
Where is whitespace being filled out of fear instead of intention?

**PERCEIVED VALUE**
Would increasing padding in any area make the design feel more premium? Where?
Are there dense areas that could be split across two sections instead of one?

Give me specific pixel recommendations. If using a component library, name which spacing tokens to use.

---

## 04 — Color And Contrast Stress Test

*Tests whether your palette is doing strategic work or just decorating the screen with random choices.*

Run a full color and contrast audit on this design. Go through each layer:

**PALETTE LOGIC**
How many colors are actively in use? List them.
Is there a clear dominant, secondary, and accent structure, or are the colors roughly equal weight?
Do any colors feel like they were added "just because"?

**EMOTIONAL SIGNAL**
What does this palette communicate emotionally? (e.g., clinical, warm, energetic, trustworthy, playful)
Is that the right signal for the product and audience?
Is there any tension between what the colors say and what the product promises?

**ACCESSIBILITY**
Flag any text and background combinations that fall below WCAG AA (4.5:1 for body, 3:1 for large text).
Are interactive elements distinguishable from non-interactive ones?

**SOPHISTICATION**
Is the accent color being overused? A color used everywhere is an accent color used nowhere.
Would swapping any color for a muted or desaturated version increase perceived quality?

---

## 05 — Why Does This Look Cheap?

*Skip the generic feedback loop. Names the specific culprits of low production value and ranks your 3 highest ROI fixes.*

> **Run this last — after prompts 01–04, this gives you the final ROI-ranked action list.**

Forget the positives for now. I need a brutally honest diagnosis. Look at this design and answer:

**THE DIAGNOSIS**
Name the 3 specific reasons this looks underdeveloped, low budget, or unfinished.
For each reason, tell me: what visual signal is creating that impression?

**THE ROOT CAUSE**
Is the core problem typography, spacing, color, layout, component quality, or consistency?
If you had to fix only ONE thing that would immediately shift the perceived quality, what is it?

**THE 10X TREATMENT**
Give me the 3 changes that would make this design look like it cost 10x more to produce.
Order them by impact. For each: what specifically changes, and why does that signal premium quality?

**WHAT TO KEEP**
Name one thing in this design that is already working well and should not be changed.

Be direct. I want a design doctor, not a design cheerleader.