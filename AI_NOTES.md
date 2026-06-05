Obszary wykorzystania AI
### 1. Uproszczenie logiki aktualizacji salda konta

Podczas implementacji usuwania transakcji zauważyłem, że początkowo napisałem kilka bardzo podobnych metod odpowiedzialnych za aktualizację salda konta. Kod działał poprawnie, ale miał sporo podobnych metod i w nich powtarzającą się logikę oraz tak zwaną "ifologie"

AI zaproponowało wykorzystanie metody:
```
private TransactionType reverse(TransactionType transactionType) {
    return transactionType == TransactionType.INCOME
            ? TransactionType.EXPENSE
            : TransactionType.INCOME;
}
```

Dzięki temu mogłem wykorzystać jedną metodę odpowiedzialną za aktualizację salda podczas dodawania i usuwania
transakcji

### 2. Filtrowanie transakcji

AI pomogło w przygotowaniu zapytania @Query służącego do filtrowania transakcji po opcjonalnych parametrach:

from
to
category

### 3. Wyliczanie podsumowania wydatków

AI pomogło w przygotowaniu metody agregującej wydatki według kategorii przy użyciu strumienia:

```
private Map<String, BigDecimal> calculateExpensesByCategory(List<AccountTransaction> transactions) {
        return transactions.stream()
                .filter(transaction -> transaction.getType() == TransactionType.EXPENSE)
                .collect(Collectors.groupingBy(
                        AccountTransaction::getCategory,
                        Collectors.mapping(
                                AccountTransaction::getAmount,
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))));
    }
```

### 4. Analiza przypadków testowych

AI pomogło w identyfikacji istotnych przypadków testowych, które warto uwzględnić podczas testowania aplikacji. Na
tej podstawie przygotowałem testy pokrywające najważniejsze elementy działania systemu

### 5. Eksport transakcji do pliku CSV

AI pomogło w zaprojektowaniu oraz implementacji funkcjonalności eksportu transakcji konta do pliku CSV
