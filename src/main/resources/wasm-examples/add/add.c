#include <stdio.h>
// can be tested by doing python3 -m http.server in this folder and opening http://localhost:8000/a.out.html in browser

int main() {
    int a = 5;
    int b = 5;

    // the following prints to web console if it is commented out
    // printf("Result: %d\n", (a+b));

    return a+b;
}