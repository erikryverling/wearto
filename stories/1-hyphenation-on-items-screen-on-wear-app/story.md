# 1 Hyphenation on Items screen in Wear app

## Description
As a user I want to get proper hyphenation on the Items screen in the Wear app to be able to more
easily read the items.

### Rules
- Use the hyphenation rules of the system language the user has choosen.
- If hard to determine the exact hyphenation rule, make sure to at least line break between full
  words

### Examples of incorrect rendering
These are some examples of swedish words with their current rendering and their expected rendering.
Note! The "\n" represent a line break.
- Current: "Matlagnings\ngrädde". Expected: "Matlagnings-\ngrädde".
- Current: "Hushållspappe\nr". Explected: "Hushålls\n-papper".
- Current: "Diskmaskinsta\nbletter". Explected: "Diskmaskins\n-tabletter".
- Current: "Kolsyrepatrone\nr". Explected: "Kolsyre-patroner".

## Definition of done
- The above examples are rendered as expected
- No other items are rendered any diffrently than the onces with incorrect rendering
- The user has visually verified that it looks correct on Wearable emulator
