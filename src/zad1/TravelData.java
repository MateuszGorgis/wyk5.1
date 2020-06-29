package zad1;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

class TravelData {
    private List<List<String>> dataList = new LinkedList<>();
    private List<String> results = null;
    private Map<String, List<String>> allResults = new HashMap<>();

    TravelData(File path) {
        try {
            File file = new File(path + "/dane");
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] splitStrings = line.split("\\t");
                List<String> temporaryList = new LinkedList<>(Arrays.asList(splitStrings));
                dataList.add(temporaryList);
            }
        } catch (FileNotFoundException e) {
            System.err.println("File not found");
        }
    }

    List<String> getOffersDescriptionsList(String loc, String dateFormat) {
        results = new LinkedList<>();
        String[] splitLocale = loc.split("_");

        Locale localeSaved;
        if (splitLocale.length > 1) {
            localeSaved = new Locale(splitLocale[0], splitLocale[1]);
        } else {
            localeSaved = new Locale(splitLocale[0]);
        }

        SimpleDateFormat simpleDateFormat = (SimpleDateFormat) DateFormat.getDateInstance();
        simpleDateFormat.applyPattern(dateFormat);

        for (List<String> list : dataList) {
			StringBuilder result = new StringBuilder();
            String[] tokensTmp = list.get(0).split("_");
            Locale locale;

            if (tokensTmp.length > 1) {
                locale = new Locale(tokensTmp[0], tokensTmp[1]);
            } else {
                locale = new Locale(tokensTmp[0]);
            }

            Locale.setDefault(locale);
            Locale[] locales = Locale.getAvailableLocales();
            Locale localeTranslate = null;

			for (Locale value : locales) {
				if (value.getDisplayCountry().equals(list.get(1)))
					localeTranslate = value;
			}
			if (localeTranslate != null) {
				result.append(localeTranslate.getDisplayCountry(localeSaved));
			}

			try {
				Date departureDate;
				departureDate = simpleDateFormat.parse(list.get(2));
                result.append("	").append(simpleDateFormat.format(departureDate));

				Date returnDate;
				returnDate = simpleDateFormat.parse(list.get(3));
                result.append("	").append(simpleDateFormat.format(returnDate));

                ResourceBundle translations = ResourceBundle.getBundle("zad1.LabelsBundle", localeSaved);
                result.append("	").append(translations.getString(list.get(4)));

                NumberFormat numberFormat = NumberFormat.getInstance(locale);
                Number price = numberFormat.parse(list.get(5));
                result.append("	").append(numberFormat.format(price));

                Currency currency = Currency.getInstance(list.get(6));
                result.append("	").append(currency.getCurrencyCode());

                results.add(result.toString());
            } catch (ParseException e) {
                System.err.println("Parse Exception");
                System.exit(2);
            }
        }
        allResults.put(loc, results);
        return results;
    }

    List<String> getResults() {
        return results;
    }

    Map<String,List<String>> getAllResults() {
        return allResults;
    }
}
