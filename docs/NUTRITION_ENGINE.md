# Nutrition Engine Design

## Key rule

Nutrition targets must not be fixed for the whole pregnancy. They should adjust based on pregnancy stage and user context.

Inputs:

- Pregnancy week
- Trimester
- Current weight
- Pre-pregnancy weight
- Height
- Pregnancy type
- Symptoms
- Medical conditions
- Allergies
- Dietary restrictions
- Supplements already prescribed/taken

## Nutrients to track in MVP

- Protein
- Folate
- Iron
- Calcium
- Vitamin D
- Vitamin B12
- Iodine
- Fiber
- Omega-3
- Choline
- Water/hydration, optional in MVP

## Stage priorities

### First trimester, weeks 1-13

Common priorities:

- Folate
- Iodine
- Vitamin B6 where nausea is present
- Hydration
- Protein tolerance
- Gentle, nausea-friendly meals

Common challenges:

- Nausea
- Vomiting
- Food aversions
- Fatigue
- Low appetite

### Second trimester, weeks 14-27

Common priorities:

- Protein
- Iron
- Calcium
- Vitamin D
- Omega-3
- Fiber

Common challenges:

- Increased appetite
- Constipation
- Heartburn

### Third trimester, weeks 28-40+

Common priorities:

- Protein
- Iron
- Calcium
- Vitamin D
- Choline
- Fiber
- Hydration

Common challenges:

- Heartburn
- Constipation
- Smaller meal capacity
- Swelling

## Target calculation strategy

Use a deterministic function:

```text
NutritionTargets = f(pregnancyWeek, trimester, currentWeightKg, prePregnancyWeightKg, heightCm, pregnancyType, medicalConditions)
```

Do not let AI invent numeric targets. AI may explain the targets and suggest foods.

## Food suggestions

Food suggestions must respect:

- Allergies
- Vegetarian/vegan status
- Religious restrictions if collected
- Medical conditions such as gestational diabetes
- Foods commonly avoided during pregnancy

## Weight language

Avoid:

- "You are overweight."
- "You gained too much."
- "You failed your target."

Use:

- "Your trend may be worth discussing with your gynecologist."
- "Weight changes can vary. Sudden changes should be checked by a clinician."
- "This is general guidance and not a diagnosis."

## MVP implementation recommendation

Start with a simple internal food database and simple target profiles. Add external nutrition database integration later.

