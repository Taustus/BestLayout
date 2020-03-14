# BestLayout
This project implements algorithm for finding sub-best layout for keyboard.
## Theory
Imagine your phone has 9 buttons (8 with number and 9th is spacebar). Under every button with number you have some letters.
This algorithm finds best layout for storage with 6k Russian words (can be bigger, but it will require more time).
Best layout is layout, that has minimum KSPC(google it) on all words in storage.
Pseudocode of an algorithm:
1. Set n and depth and initialize temporary_depth
2. Generate n layout
3. Find layout with best KSPC among all layouts and remember index
4. Generate n layouts depending on best layout on previous step and make temporary_depth++
5. Repeat steps 3-5 until temporary_depth != depth
6. Exclude previous best layout at index and repeat steps 3-6 until index != n-1
## How to stop an algorithm
For current version there's no option for interrupting proram, except you run project in debug mode and set breakpoint at any place
in 'recursion' method, then take 'solution_lowest' field as your current best solution.
