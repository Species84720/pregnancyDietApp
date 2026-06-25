# Pregnancy Safety Rules

## Core rule

Safety warnings must be implemented in app/backend logic, not only AI.

## Red-flag symptoms

Trigger urgent guidance when user logs:

- Vaginal bleeding
- Severe abdominal pain
- Severe headache
- Vision changes
- High fever
- Fainting
- Chest pain
- Severe vomiting
- Cannot keep fluids down
- Sudden swelling of face or hands
- Allergic reaction symptoms
- Reduced fetal movement in later pregnancy

## Example warning copy

```text
You reported a symptom that may need urgent medical advice. This app cannot assess emergencies. Please contact your gynecologist, maternity unit, or local emergency services now, especially if the symptom is severe, sudden, or worsening.
```

## Severity guidance

Severity 8-10 should be treated cautiously. If combined with risky symptom types, show urgent warning.

## Medication and supplement safety

The app and AI must not:

- Tell users to stop prescribed medication
- Tell users to start medication
- Change dosage
- Replace gynecologist advice

Allowed:

- Track prescribed supplements
- Remind user to take logged supplements
- Say "ask your gynecologist before changing this"

