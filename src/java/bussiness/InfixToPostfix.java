package pckgTest;

import java.text.DecimalFormat;
import java.util.Arrays;

public class InfixToPostfix {

    static final boolean INVALID_EXPRESSION = false;

    String[] stack;
    int top = -1;
    double[] stack_double;
    int top_int = -1;

    void push(String item) {
        stack[++top] = item;
    }

    String pop() {
        return stack[top--];
    }

    void push_num(double item) {
        stack_double[++top_int] = item;
    }

    double pop_int() {
        return stack_double[top_int--];
    }

//retorna precedencia de los operadores
    int precedence(String symbol) throws Exception {
        switch (symbol) {
            case "+":
            case "-":
                return 2;
            case "*":
            case "/":
                return 3;
            case "^":
                return 4;
            case "(":
            case ")":
            case "#":
                return 1;
            default:
                throw new Exception("Invalid Character");
        }
    }

    public static boolean isNumeric(String str) {
        if (null == str) {
            return false;
        }
        try {
            double d = Double.parseDouble(str);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    private void CleanCommas(String[] arr) {
        for (int i = 0; i < arr.length; i++) {
            arr[i] = arr[i].replace(",", "");
        }
    }

//convierte expresion fija en postfija
    boolean convert(String infix[], String postfix[]) throws Exception {
        int i, j = 0;
        String symbol;
        stack = new String[infix.length];
        stack[++top] = "#";
        CleanCommas(infix);
        try {
            for (i = 0; i < infix.length; i++) {
                symbol = infix[i];
                if (isNumeric(symbol)) {
                    postfix[j] = symbol;
                    j++;
                } else {
                    if ("(".equals(symbol)) {
                        push(symbol);
                    } else {
                        if (")".equals(symbol)) {
                            while (!"(".equals(stack[top])) {
                                postfix[j] = pop();
                                j++;
                            }
                            pop();//pop a (
                        } else {
                            if (precedence(symbol) > precedence(stack[top])) {
                                push(symbol);
                            } else {
                                while (precedence(symbol) <= precedence(stack[top])) {
                                    postfix[j] = pop();
                                    j++;
                                }
                                push(symbol);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            stack = null;
            top = -1;
            throw e;
        }
        while (!"#".equals(stack[top])) {
            postfix[j] = pop();
            j++;
        }
        stack = null;
        top = -1;
        return true;
    }

//evalua expresion posfija
    double evaluate(String[] postfix) throws Exception {
        String ch;
        int i = 0;
        double operand1, operand2, res;
        stack_double = new double[postfix.length];

        try {
            while (i < postfix.length && postfix[i] != null) {
                ch = postfix[i++];

                if (ch.compareTo("(") == 0 || ch.compareTo(")") == 0) {//este caracter no ha de aparecer en posfija, si lo hace el input esta incorrecto
                    throw new Exception("Invalid Character");
                }

                if (isNumeric(ch)) {
                    push_num(Double.parseDouble(ch)); //Push al operando
                } else {
                    //Operador, pop a dos operandos 
                    operand2 = pop_int();
                    operand1 = pop_int();

                    switch (ch) {
                        case "+":
                            push_num(operand1 + operand2);
                            break;
                        case "-":
                            push_num(operand1 - operand2);
                            break;
                        case "*":
                            push_num(operand1 * operand2);
                            break;
                        case "/":
                            push_num(operand1 / operand2);
                            break;
                        case "^":
                            push_num((int) Math.pow(operand1, operand2));
                            break;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            stack_double = null;
            top_int = -1;
            throw e;
        }
        res = stack_double[top_int];
        stack_double = null;
        top_int = -1;
        return res;
    }

    static boolean isOperator(char symbol) {
        switch (symbol) {
            case '+':
            case '-':
            case '*':
            case '/':
            case '^':
            case '(':
            case ')':
                return true;
            default:
                return false;
        }
    }
    
    static boolean isOperator(String symbol) {
        switch (symbol) {
            case "+":
            case "-":
            case "*":
            case "/":
            case "^":
                return true;
            default:
                return false;
        }
    }

    static boolean isSeparator(char symbol) {
        return symbol == '.' || symbol == ',';
    }

    static boolean isValid(char symbol) {
        return Character.isDigit(symbol) || isOperator(symbol) || isSeparator(symbol);
    }

    /*Validaciones:
     -[0] elementos de la lista diferentes de null
     - numeros separados por comas cada 3 dígitos, aplicamos string split(',') para obtener cada seccion del numero
        .[1] solo la primer sección puede ser diferente a 3 dígitos(ej: 12,000 对 <--> 12,00 错  12,1000 错 )
        .[2] la primer sección no puede tener mas de 3 digitos (ej: 1,256,000 对 <--> 1256,000 错 )
        .[2] la primer sección no puede ser de 0 digitos (ej: ,450 错)
        .[3] solo la ultima seccion puede tener puntos (ej: 1,256.05 对 <--> 1,23.5,9 错)
     -[4] solo puede haber un punto por numero
     -[5] el numero no puede empezar ni terminar en '.' o ','
     -[6] solo caracteres validos: numeros, operadores{+, -, *, /, ^} y puntos o comas
     -[7] caracteres de operadores deben ser un solo caracter (ej: '+' 对 <--> '+/' 错)
     -[8] la lista no puede empezar ni terminar con operadores
     - si piensa en alguna otra que pueda quebrar el programa programela aqui, o me avisa y con gusto la añado
    
     definicion de terminologia:
     sección: cada parte de un numero separado por sus comas (ej: 1,345,980 consta de 3 secciones)
     cada retorno de error marca con un numero a cual validacion corresponde
     */
    public static boolean NumberValidator(String[] arr) {
        if (arr == null || arr.length == 0) {//validación obvia
            return INVALID_EXPRESSION;
        }
        String[] sections;
        boolean pointReached = false;
        int numCount = 0, sectionIdx = 0;
        char[] numDigits;
        if (isOperator(arr[0]) || isOperator(arr[arr.length - 1])) {//[8] si la expresion empieza o termina con operadores
            return INVALID_EXPRESSION;
        }
        for (String num : arr) {//leer cada entrada de la lista recibida en el web service
            numCount = sectionIdx = 0;
            pointReached = false;
            if (num == null) {//[0]
                return INVALID_EXPRESSION;
            }
            num = num.replace(" ", ",");
            numDigits = num.toCharArray();
            if (isSeparator(numDigits[0])
                    || isSeparator(numDigits[numDigits.length - 1]))//[5] inicio o final invalido
            {
                return INVALID_EXPRESSION;
            }
            if (isOperator(numDigits[0])) {//[7] operadores deben estar solos
                if (numDigits.length > 1) {
                    return INVALID_EXPRESSION;
                }//si es un operador, y esta solito, no es necesario seguir chequeando esta entrada
                continue;
            }
            sections = num.split(",");
            for (int i = 0; i < numDigits.length; i++) {//leer cada caracter del numero
                if (!isValid(numDigits[i]))//[6] verificar caracteres sean validos
                {
                    return INVALID_EXPRESSION;
                }
                numCount += isSeparator(numDigits[i]) ? 0 : 1;//no contar los separadores como numeros
                if (sections.length != 1) { //SI HAY COMAS
                    if (numDigits[i] == ',') {//hemos leido ya toda una sección
                        if (sectionIdx == 0) {//primer sección
                            if (numCount < 1 || numCount > 3)//[2] sección inválida
                            {
                                return INVALID_EXPRESSION;
                            }
                        } else if (numCount != 3)//[1] sección inválida
                        {
                            return INVALID_EXPRESSION;
                        }
                        sectionIdx++;
                        numCount = 0;
                    }
                    if (numDigits[i] == '.') {
                        if (sectionIdx != sections.length - 1)//[3] si el punto no esta en la ultima seccion
                        {
                            return INVALID_EXPRESSION;
                        }
                        numCount = 0;
                    }
                    if (i == numDigits.length - 1 && !pointReached && numCount != 3) {//[1] si es el ultimo caracter del numero
                        return INVALID_EXPRESSION;
                    }
                }//CIERRE DE ANALISIS DE NUMERO CON COMAS
                //NUMERO SIN COMAS
                if (numDigits[i] == '.') {
                    if (pointReached)//[4] ya habiamos leido un punto para este numero
                    {
                        return INVALID_EXPRESSION;
                    }
                    pointReached = true;
                }
            }
        }
        return true;
    }

    public static void PROBAR(String[] infix, InfixToPostfix test) {
        try {
            String[] postfix = new String[infix.length];
            boolean valid = NumberValidator(infix);
            System.out.println("Input is valid: " + valid + " -> " + Arrays.toString(infix));
            if (!valid) {
                return;
            }
            if (test.convert(infix, postfix)) {
                DecimalFormat formatter = new DecimalFormat("#,###.00");
                
                System.out.println("Posfija: " + Arrays.toString(postfix));
                System.out.println("Res: " + formatter.format(test.evaluate(postfix)));
                System.out.println("------------------");
            }
        } catch (Exception ex) {}
    }

    public static void main(String[] args) {
        InfixToPostfix test = new InfixToPostfix();
        System.out.println(Arrays.toString(new String[]{"1", "+", "2", "*", "5.4", "-", "2"}));

        //1 + 2 * 5.4 - 2 -> debe dar 9.8
        PROBAR(new String[]{"1", "+", "2", "*", "5.4", "-", "2"}, test);

        //1 + 2 * 5 - 2 -> debe dar 9
        PROBAR(new String[]{"1", "+", "2", "*", "5", "-", "2"}, test);

        //1 + 2 * ( 5 - 2) -> debe dar 7
        PROBAR(new String[]{"1", "+", "2", "*", "(", "5", "-", "2", ")"}, test);

        //2 * ( 100 + 2,400 ) - 1,000  -> debe dar 4000
        PROBAR(new String[]{"2", "*", "(", "100", "+", "2,400", ")", "-", "1,000"}, test);

        //1.3 + 100 * ( 50,000 - 200) / 2 -> debe dar 2,490,001.3
        PROBAR(new String[]{"1.3", "+", "100", "*", "(", "50,000", "-", "200", ")", "/", "2"}, test);

        //INPUTS INVALIDOS
        //ha de pasar la validacion, pero tira excepcion por causa de parentesis mal puestos
        PROBAR(new String[]{"1", "*", "(", "2", "+", "3"}, test);

        //1 & 2 -> debe dar ERROR por caracter invalido
        PROBAR(new String[]{"1", "&", "2"}, test);//[6]

        PROBAR(new String[]{"1", null, "2"}, test);//[0]
        PROBAR(new String[]{"1,00", "+", "2"}, test);//[1]
        PROBAR(new String[]{"1234,000", "+", "2"}, test);//[2]
        PROBAR(new String[]{",1", "+", "2"}, test);//[2] [5]
        PROBAR(new String[]{"1", "+", "2,12.5,9"}, test);//[3]
        PROBAR(new String[]{"1.45.6", "+", "2"}, test);//[4]
        PROBAR(new String[]{".1", "+", "2"}, test);//[5]
        PROBAR(new String[]{"1", "++", "2"}, test);//[7]
        PROBAR(new String[]{"1", "+", "2", "-"}, test);//[8]
        PROBAR(new String[]{"/", "1", "+", "2"}, test);//[8]
    }
}
