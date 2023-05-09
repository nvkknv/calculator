import org.jetbrains.annotations.NotNull;
import java.util.Scanner;

public class Main {
    static Scanner readln = new Scanner(System.in);
    static void writeln(String s) {
        System.out.println(s);
    }
    static final int ITEM_CNT = 3; // число элементов выражения
    static final int CNT_ROMAN_PLUS = 3; // максимальное количество суммирующихся одинаковых римских цифр подряд
    static final int CNT_ROMAN_MINUS = 1; // максимальное количество вычитающихся одинаковых римских цифр подряд
    static String sNdef = " "; // игнорируемые символы между операндами
    static String sArab = "0123456789";
    static String sRoman = "IVXLCDM";
    static String minusDigit = "IXC"; // вычитаемые цифры
    static String sOperation = "+-/*";


    static class MyException extends ArithmeticException
    {
        MyException(String msg) {
            super(msg);
        }
    }

    enum tVid {Deny, Ndef, Arab, Roman, Operation} // вид символа, элемента

// массив значений римских цифр
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

// превращает число в строку - римское число
    static @NotNull String toRoman(int value) {
        int k, arabDigit;
        char digit1, digit5, digit10;
        StringBuilder result = new StringBuilder();

        if (value < 1)
            throw new MyException("Римское число должно быть положительным");

        if (value > 3999)
            throw new MyException("Римское число не может быть более 3999");

// последовательно, начиная со старшего разряда тысяч, преобразовать цифры числа в римские цифры
        k = 1000;  // начальное значение делителя - для тысяч

        while (k >= 1) {
// определить набор трёх римских цифр для разрядов тысяча, сотня, десяток, единица
            switch (k) {
                case 1000 -> {
                    digit1 = 'M';
                    digit5 = ' ';
                    digit10 = ' ';
                }
                case 100 -> {
                    digit1 = 'C';
                    digit5 = 'D';
                    digit10 = 'M';
                }
                case 10 -> {
                    digit1 = 'X';
                    digit5 = 'L';
                    digit10 = 'C';
                }
                default -> {
                    digit1 = 'I';
                    digit5 = 'V';
                    digit10 = 'X';
                }
            }

// получить цифру
            arabDigit =  value / k % 10;
// дописать римские цифры в результат в соотвествии с цифрой и её разрядом
            switch (arabDigit) {
                case 1, 2, 3 -> result.append(String.valueOf(digit1).repeat(arabDigit));
                case 4 -> {
                    result.append(digit1);
                    result.append(digit5);
                }
                case 5, 6, 7, 8 -> {
                    result.append(digit5);
                    result.append(String.valueOf(digit1).repeat(arabDigit - 5));
                }
                case 9 -> {
                    result.append(digit1);
                    result.append(digit10);
                }
            }

            k = k / 10;
        }

        return result.toString();
    }

// возвращает числовое значение римской цифры
    static int valueOfRomanDigit(char c) {
        return switch (c) {
            case 'I' -> 1;
            case 'V' -> 5;
            case 'X' -> 10;
            case 'L' -> 50;
            case 'C' -> 100;
            case 'D' -> 500;
            case 'M' -> 1000;
            default -> 0;
        };
    }

// возвращает число - значение строки - римского числа
// static @NotNull String toRoman(int value)
    static int toArab(@NotNull String roman) {
        int result = 0;
        int valueC, valueLastC = 0, cntLastC = 0;
        char c, lastC = ' ';

        for (int iRoman = 0; iRoman < roman.length(); iRoman++) {
            c = roman.charAt(iRoman);
            valueC = valueOfRomanDigit(c);

            if (iRoman == 0) {
                lastC = c;
                valueLastC = valueC;
                cntLastC = 1;
                continue;
            }

            if (valueC == valueLastC) {
                if (cntLastC == 0) {
                    throw new MyException("В римском числе вычитать можно только из последней из одинаковых цифр");
                }
                else {
                    cntLastC++;
                    continue;
                }
            }

            if (valueC > valueLastC) {
// вычитание предыдущих и суммирование текущей
                if (cntLastC > CNT_ROMAN_MINUS) {
                    throw new MyException("В римском числе может быть не более %s вычитаемых '%s' подряд".formatted(CNT_ROMAN_MINUS, c));
                }

// вычитаться может только цифра I, X, C и соответствующая разряду большей цифры
                if ((valueC <= 10 * valueLastC) && (minusDigit.indexOf(lastC) >= 0)) {
                    result = result + valueC - valueLastC * cntLastC;
                    lastC = c;
                    valueLastC = valueC;
                    cntLastC = 0;
                }
                else
                    throw new MyException("В римском числе после %s не может быть %s".formatted(lastC, c));
            }
            else {
// суммирование предыдущих цифр
                if (cntLastC > CNT_ROMAN_PLUS) {
                    throw new MyException("В римском числе может быть не более %s '%s' подряд".formatted(CNT_ROMAN_PLUS, c));
                }

                result = result + valueLastC * cntLastC;
// запоминание текущей цифры для последующего сравнивания
                lastC = c;
                valueLastC = valueC;
                cntLastC = 1;
            }

        }

        result = result + valueLastC * cntLastC;
        return result;
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

        while (true) {
            System.out.print("Выражение (пусто - завершить работу): ");
            input = readln.nextLine();

            if (input.equals(""))
                break;

            writeln("%s = %s".formatted(input, calc(input)));
        }
/*
        for (int i = 1; i < 1001; i++) {
            if (i != toArab(toRoman(i)))
                writeln("%s %s %s".formatted(i, toArab(toRoman(i)), toRoman(i)));
        }
*/
    }
}