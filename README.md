# Matrix Toolkit

The Matrix Toolkit is a Java learning project that implements a core matrix algebra functionality from scratch. Specifically, this program operates with a command line interface that allows the user to perform certain matrix algebra operations. I wrote this project to better my undersrtanding of linear algebra and design practices in Java.

## Features

- Matrix creation and basic operations (addition, subtraction, scalar multiplication)
- Transpose and elementwise functions
- Determinant and rank calculations
- Gaussian elimination to row echelon and reduced row echelon form
- Forward and backward substitution
- LU decomposition with partial pivoting
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

I created this project to strengthen my knowledge of Java fundamentals. I wanted to practice using some common dependencies such as JUnit 5, SLF4J, and Jackson as well as refresh my knowledge of linear algebra. My goal outside this project is to review all of the mathematics I need to understand machine learning and a linear algebra project seemed a good first step.

## AI Usage Note

A couple of the numerical methods in this project, such as the QR and LU decomposition methods, were initially written by OpenAI's ChatGPT (GPT-5 model). I integrated and tested these algorithms myself. I think it's important to demonstrate that I can leverage AI in software development to speed up the process while still being responsible for the correctness, maintainability, and design of my project.

## License

This project is open source under the MIT License.