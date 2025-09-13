# Matrix Toolkit

A Java learning project that implements core matrix algebra functionality from scratch.  
This project was built as a way to deepen my understanding of linear algebra, numerical methods, and good object-oriented design practices in Java.

## Features

- Matrix creation and basic operations (addition, subtraction, scalar multiplication)
- Transpose and elementwise functions
- Determinant and rank calculations
- Gaussian elimination to row echelon and reduced row echelon form
- LU decomposition with partial pivoting
- Forward and backward substitution
- QR decomposition via Householder reflections
- Norms (L1, L∞, Frobenius, trace)
- Caching and utility helpers
- JSON-based persistence (save/load matrices)
- JUnit test coverage with custom `assertMatrixEquals` and `assertMatrixEqualsApprox`

## Technologies

- **Language:** Java 17  
- **Build tool:** Maven  
- **Logging:** SLF4J  
- **Testing:** JUnit 5  

## Why I Built This

I created this project to strengthen my Java fundamentals (collections, exceptions, unit tests, Maven project structure) and
to gain practical experience implementing algorithms from linear algebra.

## AI Usage Note

Some methods in this project, such as QR decomposition (Householder reflections) and LU decomposition (with partial pivoting),
were implemented with guidance from OpenAI’s ChatGPT (GPT-5 model). I integrated, tested, and documented these algorithms myself.

I’ve chosen to explicitly credit this AI assistance because I see effective use of AI as an engineering skill, especially
knowing when and how to leverage it to accelerate development, while still being responsible for correctness, maintainability,
and design.

## License

This project is open source under the MIT License.