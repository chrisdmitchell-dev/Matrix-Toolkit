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
## To Do For This Version

- [ ] When exiting, check whether all matrices in memory have been saved to disk.
      Verify if you want to exit without saving everything.

- [ ] Add an option to save all matrices in memory to disk. Have one file be able
      to contain multiple matrices.
      
- [ ] Add throws for Objects.requireNonNull statements (NullPointerException)

- [ ] Enable exception handling in anything that calls something in Matrix.java that throws an exception

- [ ] Make sure all javadocs have the appropriate throws statements.

- [ ] Write tests to test for thrown exceptions

- [ ] Make sure ADD, SUBTRACT, and MULTIPLY in Main.java are structured the same along with
      any other matrix operations.

## To Do For Future Versions

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


/* TODO (Matrix cleanup + maintainability roadmap)
 * 
 * 1) Move long TODOs out of Matrix.java
 *    - Create docs/DEV_NOTES.md or BACKLOG.md and store detailed notes there.
 *    - Leave a short tracker comment + link at the top of Matrix.java.
 *
 * 2) Restructure Matrix.java layout with clear section banners:
 *    - Fields (state, cache, logger)
 *    - Constructors & Factories
 *    - Accessors / Metadata
 *    - Core API (arithmetic: add, subtract, multiply, transpose, inverse…)
 *    - Facts (determinant, rank, nullity)
 *    - Decompositions & Cached Results (REF, RREF, LU, QR)
 *    - Equality / Hashing / String Representations
 *    - Internal Helpers (index checks, cofactor, minor, recursive det…)
 *
 * 3) Split responsibilities:
 *    - Keep MatrixUtils for I/O-free helpers (copy, scale, format).
 *    - Move determinant helpers (cofactor, minor, recursive det) → Determinants.java (package-private).
 *    - Move bucketIndex or comparison policies → MatrixComparators.java or MatrixUtils.
 *    - Keep decompositions in separate packages (algo.reducedform, algo.lu, algo.qr).
 *
 * 4) Document cached results clearly:
 *    - Public accessors should state: “Returns immutable snapshot, recomputed when matrix mutates.”
 *    - Keep @JsonIgnore on cache + version fields.
 *
 * 5) Tighten Javadoc:
 *    - Public: state preconditions, postconditions, error cases.
 *    - Use @implNote for algorithm details (pivoting, tolerance policy).
 *    - Avoid narrating the code line-by-line.
 *
 * 6) Group related methods together:
 *    - Arithmetic together (add, subtract, multiply…).
 *    - Shape/facts together (isEmpty, isSquare, isInvertible, rank, nullity).
 *    - Cached decomposition APIs together (ref, rref, lu, qr).
 *
 * 7) Add small guard helpers:
 *    - checkNotReadOnly()
 *    - checkIndex(row, col)
 *    - checkSquareNonEmpty()
 *    → Use these in setters / arithmetic to shrink boilerplate.
 *
 * 8) Normalize boolean predicates & use early returns:
 *    - isEmpty(), isSquare(), isInvertible() should be concise.
 *
 * 9) Logging hygiene:
 *    - TRACE = per-element, DEBUG = operation complete, INFO = rare.
 *    - Don’t log inside tight inner loops.
 *
 * 10) Naming + visibility:
 *    - Make Matrix final (not designed for inheritance).
 *    - Keep cache records private static.
 *    - Consider static factories for readability (Matrix.of(...)).
 *
 * 11) When adding LU/QR:
 *    - Place impl in algo.lu and algo.qr packages.
 *    - Matrix.lu()/qr() just delegate, wrap results in read-only Matrices, and cache with version.
 */

/*
TODO (Matrix Project — next ML-focused operations)

[X] RANK & NULLITY
    [X] rank(): compute from RREF (count non-zero rows)
    [X] nullity(): nCols - rank()
    [X] Tests: rank(null) + nullity == nCols; known dependent examples

[ ] TRACE & NORMS
    [X] trace(): sum diagonal (square-only check)
    [X] Vector norms: L1, Linf
    [ ] Vector norms: L2
    [X] Matrix norm: Frobenius (sqrt(sum of squares))
    [X] Tests: hand-checked small matrices/vectors

[X] ELEMENTWISE & MAP
    [X] hadamard(Matrix B): elementwise multiply (size check)
    [X] map(DoubleUnaryOperator f): apply f to each entry
    [X] Tests: compare against manual loops

[ ] TRIANGULAR SOLVES
    [ ] forwardSub(L, b): L lower-triangular with nonzero diag
    [ ] backSub(U, b): U upper-triangular with nonzero diag
    [ ] Tests: solve small systems; check A·x ≈ b

[ ] LU DECOMPOSITION (with partial pivoting)
    [ ] lu(): return {L, U, pivots, sign}; cache result
    [ ] solve(b): use LU + (forwardSub/backSub)
    [ ] det(): reuse LU (det = sign * prod(diag(U)))
    [ ] Tests: P·A ≈ L·U; solve random systems vs inverse(b) baseline

[ ] QR DECOMPOSITION (Householder)
    [ ] qr(): return {Q, R} with Q orthonormal
    [ ] solveLeastSquares(b): tall A; minimize ||Ax - b||2
    [ ] Tests: QᵀQ ≈ I, A ≈ Q·R; LS residual minimal vs normal equations

[ ] CHOLESKY (SPD)
    [ ] chol(): A = L·Lᵀ for symmetric positive-definite A
    [ ] solveSPD(b): use Cholesky solves
    [ ] Tests: reconstruct A; compare solves vs LU

[ ] STATS / PREPROCESSING
    [ ] columnMeans(), columnStds()
    [ ] centered(): subtract column means
    [ ] standardized(): z-score by column
    [ ] covariance(): (1/(m-1)) * centered(A)ᵀ * centered(A)
    [ ] correlation(): from covariance + stds
    [ ] Tests: small dataset with known results

[ ] PROJECTIONS & ORTHONORMAL BASES
    [ ] orthonormalize(): Modified Gram–Schmidt on columns
    [ ] projectOntoColSpace(b): use QR
    [ ] Tests: ‖b - projection‖ ≤ ‖b - any other Ax‖

[ ] POWER ITERATION (top eigenpair)
    [ ] powerIteration(int maxIters, double tol): returns (λ, v)
    [ ] Tests: Rayleigh quotient close to λ; ‖A v - λ v‖ small

[ ] PSEUDOINVERSE & RIDGE UTILITIES
    [ ] pinv(): initial version via QR (full-rank); later via SVD
    [ ] addLambdaI(double λ): A + λI helper
    [ ] Tests: A·pinv(A)·A ≈ A; ridge solve sanity checks

[ ] SLICING & STACKING
    [ ] slice(r0,r1,c0,c1): half-open ranges
    [ ] hstack(B), vstack(B): dimension checks
    [ ] Tests: shapes & contents exact

// Notes:
/// - Cache decompositions and invalidate on mutation.
/// - Prefer solve() over inverse() for numerical stability.
/// - Add randomized tests alongside deterministic ones.
*/
