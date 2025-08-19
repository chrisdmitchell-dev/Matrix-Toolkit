# Backlog

## Next Steps

1. Freeze the scope
    - Forget Rational numbers for now — stick to doubles.
    - Ignore generics, advanced algorithms, CLI expansions, and file persistence changes.
    - You’re only building the core math engine.
2. Pick a minimum algebra set
    - Create a matrix from data you give it.
    - Retrieve and set values.
    - Perform: addition, subtraction, scalar multiplication, transpose, matrix multiplication.
    - (Optional for now) Identity matrix, equality comparison with a tolerance.
3. Work in test-driven slices
    - Pick one operation, e.g., addition.
    - Write a simple test with a known answer.
    - Implement just enough code to pass it.
    - Move to the next operation.
4. Proof of life milestone
    - You can multiply a 2×3 by a 3×2 and get the expected numbers.
    - You can confirm it with a test, not by eyeballing it.
5. Keep a “Later” list
    - Put it in a later.txt or backlog.md.
    - Don’t touch it until your milestone passes all tests.

---

## To Do

- [ ] Print the matrix and augmented matrix as vectors.

```
public static final String SUBSCRIPT_DIGITS = "₀₁₂₃₄₅₆₇₈₉";
public static String subScriptString(int value) {
	StringBuilder subscriptString = new StringBuilder();
    for (char ch : Integer.toString(value).toCharArray()) {
    	int digit = ch - '0';
        subscriptString.append(SUBSCRIPT_DIGITS.charAt(digit));
    }
    return subscriptString.toString();
}
```

- [ ] Add constants to allow for the setting of short/pretty print and exactEquals/approxEquals/equals

