# Phase 7: Pregnancy-Week-Aware Nutrition Engine

## Goal
Build a nutrition engine that changes by pregnancy week, trimester, and weight.

## Scope
1. Build a local nutrition engine module.
2. Nutrition targets must consider:
   - pregnancy week
   - trimester
   - current weight
   - pre-pregnancy weight if available
   - height if available
   - pregnancy type
   - dietary restrictions
   - medical conditions
3. Track at least:
   - protein
   - folate
   - iron
   - calcium
   - vitamin D
   - vitamin B12
   - iodine
   - fiber
   - omega-3
   - choline
   - water placeholder
4. Create pregnancy stage profiles:
   - trimester 1
   - trimester 2
   - trimester 3
5. Store daily nutrition summaries at `users/{uid}/dailyNutritionSummaries/{date}`.
6. Each summary should include:
   - date
   - pregnancyWeek
   - trimester
   - nutritionProfileVersion
   - totals
   - targets
   - gaps
   - stagePriorities
7. Add Nutrition Summary screen.
8. Show daily gaps and weekly trends.
9. Use non-shaming language.
10. Add tests for target calculation and gap detection.

## Safety rules
- Do not provide medical diagnosis.
- Do not recommend supplement dosage changes.
- Food-based suggestions are allowed.
- Doctor advice should override generic guidance.

## Acceptance criteria
- Nutrition targets respond to week/trimester/weight.
- Daily summaries are saved and displayed.
- Tests cover target calculation and gap detection.

## Required commit message
`Phase 7: Add pregnancy nutrition engine`
