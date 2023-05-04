import org.jetbrains.annotations.NotNull;

public class Main {
    static void writeln(String s) {
        System.out.println(s);
    }
    static final int ITEM_CNT = 3; // число элементов выражения
    static final int I_I = 3; // максимальное количество I подряд
    static final int I_VX = 1; // максимальное количество I перед V,X
    static String sNdef = " "; // игнорируемые символы между операндами
    static String sArab = "0123456789";
    static String sRoman = "IVX";
    static String sOperation = "+-/*";

    static class MyException extends ArithmeticException
    {
        MyException(String msg) {
            super(msg);
        }
    }

    enum tVid {Deny, Ndef, Arab, Roman, Operation} // вид символа, элемента

// класс: строка; вид её символов
    static class tVidChars {
        tVid Vid;
        String Chars;

        tVidChars(tVid aVid, String aChars) {
            Vid = aVid;
            Chars = aChars;
        }
    }

// класс: строка; вид; методы, возвращающие числовое значение строки
    static class tItem extends tVidChars {
        tItem(tVid aVid, String aChars) {
            super(aVid, aChars);
        }

        int toInt() {
            return switch (Vid) {
                case Arab -> Integer.parseInt(Chars);
                case Roman -> toArab(Chars);
                default -> throw new MyException("Элемент не содержит целое число");
            };
        }

        int toCheckInt() {
            int result;

            result = toInt();

            if (result < 1 | result > 10)
                throw new MyException("Операнд должен быть в диапазоне 1 - 10");
            else
                return result;
        }
    }

// массив с набором символов для каждого вида
    static tVidChars [] arrVidChars = {
            new tVidChars(tVid.Arab, sArab),
            new tVidChars(tVid.Roman, sRoman),
            new tVidChars(tVid.Operation, sOperation),
            new tVidChars(tVid.Ndef, sNdef)
    };

// возращает вид символа
    static tVid vidOfChar(char c) {
        tVid result = tVid.Deny;

        for (tVidChars VidChars : arrVidChars) {
            for (int iChars = 0; iChars < VidChars.Chars.length(); iChars++) {
                if (VidChars.Chars.charAt(iChars) == c) {
                    result = VidChars.Vid;
                    break;
                }
            }
            if (result != tVid.Deny)
                break;
        }

        return result;
    }

// возвращает числовое значение римской цифры
    static int valueOfRomanChar(char c) {
        return switch (c) {
            case 'I' -> 1;
            case 'V' -> 5;
            case 'X' -> 10;
            default -> 0;
        };
    }

// дописывает в конец римского числа roman символы c пока value не меньше limit,
// возвращает value, уменьшенное на сумму дописанных символов c
    static int appendArabChar(char c, int limit, int value, StringBuilder roman) {
        int charValue = valueOfRomanChar(c);

        while (value >= limit) {
            roman.append(c);
            value = value - charValue;
        }

        return value;
    }

// возвращает строку - римское число
    static @NotNull String toRoman(int value) {
        if (value < 1)
            throw new MyException("Римское число должно быть положительным");

        StringBuilder result = new StringBuilder();
// добавить X
        value = appendArabChar('X', valueOfRomanChar('X'), value, result);

        if (value >= valueOfRomanChar('X') - I_VX) {
// добавить IX
            appendArabChar('I', valueOfRomanChar('X') - I_VX, value, result);
            result.append("X");
        }
        else {
            if (value >= valueOfRomanChar('V')) {
// добавить VI
                result.append("V");
                value = value - valueOfRomanChar('V');
                appendArabChar('I', valueOfRomanChar('I'), value, result);
            }
            else {
                if (value >= valueOfRomanChar('V') - I_VX) {
// добавить IV
                    appendArabChar('I', valueOfRomanChar('V') - I_VX, value, result);
                    result.append("V");
                }
                else {
// добавить I
                    appendArabChar('I', valueOfRomanChar('I'), value, result);
                }
            }
        }

        return result.toString();
    } // toRoman

// возвращает числовое значение римского числа
    static int toArab(@NotNull String roman) {
        int result = 0;
        int cntI = 0; // количество I
        boolean bI = false, bV = false; // наличие I, V
        char c;

        for (int iRoman = 0; iRoman < roman.length(); iRoman++) {
            if (bI) {
                throw new MyException("В римском числе можно вычитать I только из последней цифры");
            }

            c = roman.charAt(iRoman);

            switch (c) {
                case 'I' -> {
                    if (cntI >= I_I) {
                        throw new MyException("В римском числе может быть не более %s 'I' подряд".formatted(I_I));
                    }

                    cntI++;
                }
                case 'V', 'X' -> {
                    if (bV) {
                        throw new MyException("В римском числе не может быть 'V' перед 'X' или 'V'");
                    }

                    if (cntI > I_VX) {
                        throw new MyException("В римском числе не может быть более %s 'I' перед '%s'".formatted(I_VX, c));
                    }

                    if (c == 'V')
                        bV = true;

                    result = result - cntI;

                    if (cntI > 0) {
                        bI = true;
                        cntI = 0;
                    }

                    result = result + valueOfRomanChar(c);
                }
            }
        }

        return result + cntI;
    } // toArab

// возвращает текстовое значение выражения, представленного в массиве элементов, содержащем число, операция, число
    static String valueOfArrItem(tItem @NotNull [] arrItem) {
        int result = 0, value;
        tVid exprVid, currVid, nextVid;
        tItem operationItem = new tItem(tVid.Ndef, "");

        if (arrItem[0] == null) {
            throw new MyException("В выражении менее %s элементов".formatted(ITEM_CNT));
        }

        exprVid = arrItem[0].Vid;
        currVid = exprVid;

        switch (currVid) {
            case Operation -> throw new MyException("1-й элемент должен быть числом");
            case Arab, Roman -> result = arrItem[0].toCheckInt();
        }

        for (int iArr = 1; iArr < arrItem.length; iArr++) {
            if (arrItem[iArr] == null)
                throw new MyException("В выражении менее %s элементов".formatted(ITEM_CNT));

            nextVid = arrItem[iArr].Vid;

            if (nextVid == tVid.Operation) {
                if (currVid == tVid.Operation) {
                    throw new MyException("В выражении не может быть 2 операции подряд");
                }
                else {
                    operationItem = arrItem[iArr];
                }
            }
            else {
                if (nextVid != exprVid) {
                    throw new MyException("Все операнды должны быть числами одной системы счисления");
                }
                else {
                    value = arrItem[2].toCheckInt();

                    result = switch (operationItem.Chars) {
                        case "+" -> result + value;
                        case "-" -> result - value;
                        case "*" -> result * value;
                        case "/" -> result / value;
                        default -> throw new MyException("Недопустимая операция '%s'".formatted(arrItem[1].Chars));
                    };
                }
            }

            currVid = nextVid;
        }

        if (exprVid == tVid.Arab)
            return String.valueOf(result);
        else
            return toRoman(result);
    }

// возвращает массив элементов, полученный преобразованием текстового выражения
    static tItem @NotNull [] parser(@NotNull String input) {
        tItem[] arrItem = new tItem[ITEM_CNT];
        char c;
        tVid vidC;
        int itemI = -1;
        tItem item = new tItem(tVid.Ndef, "");

        for (int iInput = 0; iInput < input.length(); iInput++) {
            c = input.charAt(iInput);
            vidC = vidOfChar(c);

            if (vidC == tVid.Deny)
                throw new MyException("Недопустимый символ '%s'".formatted(c));

            if (vidC == item.Vid) {
                item.Chars = item.Chars + c;
            }
            else {
                if (item.Vid != tVid.Ndef) {
                    itemI++;
                    arrItem[itemI] = item;
                    item = new tItem(tVid.Ndef, "");
                }

                if (vidC != tVid.Ndef & itemI == ITEM_CNT - 1) {
                    throw new MyException("Число элементов выражения не может быть больше %s, лишний элемент в позиции %s".formatted(ITEM_CNT, iInput + 1));
                }

                item.Vid = vidC;
                item.Chars = String.valueOf(c);
            }
        }

        if (item.Vid != tVid.Ndef) {
            itemI++;
            arrItem[itemI] = item;
        }

        return arrItem;
    }

// возвращает текстовый результат вычисления текстового выражения
    public static String calc(String input) {
        return valueOfArrItem(parser(input));
    }

    public static void main(String[] args) {
        String input;
        input = " X -X";
        writeln("%s = %s".formatted(input, calc(input)));

//        for (int i = 1; i < 22; i++) {writeln("%s %s %s".formatted(i, toArab(toRoman(i)), toRoman(i)));}
    }
}