import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.ArrayList;

public final class StringCalculator {

    public int add(String numbers){
        if (numbers.isEmpty()) return 0;

        int sum = 0;
        String regex;
        String[] new_one;

        if (numbers.startsWith("//")) {
            int end = numbers.indexOf('\n');

            if (numbers.charAt(2) == '[') {
                Pattern pattern = Pattern.compile("\\[(.*?)\\]");
                Matcher matcher = pattern.matcher(numbers.substring(0, end));
                
                ArrayList<String> delimiters = new ArrayList<>();
                while (matcher.find()) {
                    delimiters.add(matcher.group(1));
                }
                
                // Build a single regex like: \Q*\E|\Q£\E
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < delimiters.size(); i++) {
                    sb.append(Pattern.quote(delimiters.get(i)));
                    if (i < delimiters.size() - 1) {
                        sb.append("|"); // Add OR between them
                    }
                }
                regex = sb.toString();
                
            } else {
                // Fallback for single custom delimiter like //*\n
                regex = Pattern.quote(numbers.substring(2, end));
            }
                
            // Strip the header so we are left only with the numbers
            numbers = numbers.substring(end + 1);
            new_one = numbers.split(regex);

        } else {
                regex = "[,\\n]";
                new_one = numbers.split(regex);
        }

        ArrayList<Integer> negativeNumbers = new ArrayList<Integer>();

        for (int i = 0; i < new_one.length; i++) {
            int number = Integer.parseInt(new_one[i]);

            if (number < 0){
                negativeNumbers.add(number);
            }

            if (number > 1000){
                number = 0;
            }

            sum += number;
        }

        if (!negativeNumbers.isEmpty()) {
            throw new IllegalArgumentException("Negatives are not allowed: " + negativeNumbers);
        }

        return sum;
    }
}