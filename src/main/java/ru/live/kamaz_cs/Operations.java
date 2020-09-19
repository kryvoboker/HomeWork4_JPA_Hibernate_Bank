package ru.live.kamaz_cs;

import org.apache.commons.math3.util.Precision;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

public class Operations {

    EntityManagerFactory emf;
    EntityManager em;

    public void menu() {
        Scanner sc = new Scanner(System.in);
        try {
            emf = Persistence.createEntityManagerFactory("JPABank");
            em = emf.createEntityManager();
            createCurrencyExchange(); //создаю таблицу валют обходя меню
            createUsers();//создаю таблицу пользователей обходя меню
            try {
                while (true) {
                    System.out.println("1: Create money accounts");
                    System.out.println("2: Еnter the top-up amount");
                    System.out.println("3: Transfer");
                    System.out.println("4: Currency conversion");
                    System.out.println("5: Look cash from user in UAH");
                    System.out.print("-> ");

                    String s = sc.nextLine();
                    switch (s) {
                        case "1":
                            createMoneyAccount();
                            break;
                        case "2":
                            toUpAmount();
                            break;
                        case "3":
                            moneyTransfer();
                            break;
                        case "4":
                            currencyConversion();
                            break;
                        case "5":
                            getCash(sc);
                            break;
                        default:
                            return;
                    }
                }
            } finally {
                sc.close();
                em.close();
                emf.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }
    }

    private int getUserToTopUpAmount() {
        Scanner sc = new Scanner(System.in);
        System.out.println("Select user: ");
        System.out.println("1: " + NAMES[0] + "\n" + "2: " + NAMES[1] + "\n" + "3: " + NAMES[2] + "\n" + "4: " + NAMES[3]);
        String sName = sc.nextLine();
        int name = Integer.parseInt(sName);
        return name;
    }

    private String getCurrency() {
        Scanner sc = new Scanner(System.in);
        System.out.println("Select currency: ");
        System.out.println("1: USD" + "\n" + "2: EUR" + "\n" + "3: UAH");
        String sCurrency = sc.nextLine();
        int currency = Integer.parseInt(sCurrency);
        String a = null;
        for (int j = 0; j < CURRENCYS.length; j += 1) {
            a = CURRENCYS[currency - 1];
        }
        return a;
    }

    private double getAmount() {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter the amount of money: ");
        String sAmount = sc.nextLine();
        double amount = Double.valueOf(sAmount);
        return amount;
    }

    private synchronized void toUpAmount() { //пополняю счет // заранее создал готовых пользователей и готовые названия валют, в самом низу класса
        int name = getUserToTopUpAmount();
        String currency = getCurrency();
        double amount = getAmount();

        if (name == 1 || name == 2 || name == 3 || name == 4) {
            for (int i = 0; i < NAMES.length; i += 1) {
                for (int j = 0; j < CURRENCYS.length; j += 1) {
                    Transaction transaction = new Transaction(NAMES[name - 1], 0, "none", amount, currency, new Date(), 0, "none", "none", 0);

                    Query query = em.createNamedQuery("MoneyAccount.findAll", MoneyAccount.class);
                    List<MoneyAccount> m = query.getResultList();

                    for (MoneyAccount f : m) {
                        if (f.getNameOfUser().equals(NAMES[name - 1])) {
                            if (currency.equals("USD")) {
                                f.setCashInUSD(f.getCashInUSD() + amount);
                                mergeMoneyAccount(f);                                    //постоянно обновляю значения в таблице moneyaccounts
                            } else if (currency.equals("EUR")) {
                                f.setCashInEUR(f.getCashInEUR() + amount);
                                mergeMoneyAccount(f);
                            } else if (currency.equals("UAH")) {
                                f.setCashInUAH(f.getCashInUAH() + amount);
                                mergeMoneyAccount(f);
                            }
                        }
                    }
                    saveTransaction(transaction);
                    break;
                }
                break;
            }
        }
    }

    private String getUserForTransfareOut() {
        Scanner sc = new Scanner(System.in);
        System.out.println("Select user for money out: ");
        System.out.println("1: " + NAMES[0] + "\n" + "2: " + NAMES[1] + "\n" + "3: " + NAMES[2] + "\n" + "4: " + NAMES[3]);
        String sNameOut = sc.nextLine();
        int b = Integer.parseInt(sNameOut);
        String a = null;
        for (int i = 0; i < NAMES.length; i += 1) {
            a = NAMES[b - 1];
        }
        return a;
    }

    private String getUserForTransfareIn() {
        Scanner sc = new Scanner(System.in);
        System.out.println("Select user for money take: ");
        System.out.println("1: " + NAMES[0] + "\n" + "2: " + NAMES[1] + "\n" + "3: " + NAMES[2] + "\n" + "4: " + NAMES[3]);
        String sNameTake = sc.nextLine();
        int b = Integer.parseInt(sNameTake);
        String a = null;
        for (int i = 0; i < NAMES.length; i += 1) {
            a = NAMES[b - 1];
        }
        return a;
    }

    private synchronized void moneyTransfer() { //перевод денег
        String nameOut = getUserForTransfareOut();
        String currency = getCurrency();
        String nameTake = getUserForTransfareIn();
        double amount = getAmount();

        Query query = em.createNamedQuery("MoneyAccount.findAll", MoneyAccount.class);
        List<MoneyAccount> m = query.getResultList();

        for (MoneyAccount f : m) {
            if (f.getNameOfUser().equals(nameOut)) {
                if (f.getCashInEUR() >= amount || f.getCashInUAH() >= amount || f.getCashInUSD() >= amount) { //проверка на наличие денег и чтобы пользователь сам себе не пыталься переводить деньги
                    transferOut(nameOut, nameTake, amount, currency);
                    transferIn(nameOut, nameTake, amount, currency);
                }
            } else {
                    System.out.println("User do not have enough money to transfer!");
                    break;
                }
            }
        }




    private void transferOut(String nameOut, String nameIn, double amount, String currency) {
        Query query = em.createNamedQuery("MoneyAccount.findAll", MoneyAccount.class);
        List<MoneyAccount> m = query.getResultList();

        for (MoneyAccount f : m) {
            if (!nameOut.equals(nameIn)) { //если пытается сам себе перевести, то пусть попытается еще раз
                if (f.getNameOfUser().equals(nameOut)) {
                    if (currency.equals("USD")) {
                        f.setCashInUSD(f.getCashInUSD() - amount);
                        mergeMoneyAccount(f);
                    } else if (currency.equals("EUR")) {
                        f.setCashInEUR(f.getCashInEUR() - amount);
                        mergeMoneyAccount(f);
                    } else if (currency.equals("UAH")) {
                        f.setCashInUAH(f.getCashInUAH() - amount);
                        mergeMoneyAccount(f);
                    }
                    Transaction transactionOut = new Transaction(nameOut, amount, currency,
                            0, "none", new Date(), 0, "none", "none", 0);
                    saveTransaction(transactionOut);
                }
            }
        }
    }

    private void transferIn(String nameOut, String nameIn, double amount, String currency) {
        Query query = em.createNamedQuery("MoneyAccount.findAll", MoneyAccount.class);
        List<MoneyAccount> m = query.getResultList();

        for (MoneyAccount f : m) {
            if (!nameOut.equals(nameIn)) {
                if (f.getNameOfUser().equals(nameIn)) {
                    if (currency.equals("USD")) {
                        f.setCashInUSD(f.getCashInUSD() + amount);
                        mergeMoneyAccount(f);
                    } else if (currency.equals("EUR")) {
                        f.setCashInEUR(f.getCashInEUR() + amount);
                        mergeMoneyAccount(f);
                    } else if (currency.equals("UAH")) {
                        f.setCashInUAH(f.getCashInUAH() + amount);
                        mergeMoneyAccount(f);
                    }
                    Transaction transactionTake = new Transaction(nameIn, 0, "none", amount,
                            currency, new Date(), 0, "none", "none", 0);
                    saveTransaction(transactionTake);
                }
            }
        }
    }

    private int getUserForConversation() {
        Scanner sc = new Scanner(System.in);
        System.out.println("Select user for conversion: ");
        System.out.println("1: " + NAMES[0] + "\n" + "2: " + NAMES[1] + "\n" + "3: " + NAMES[2] + "\n" + "4: " + NAMES[3]);
        String sName = sc.nextLine();
        int name = Integer.parseInt(sName);
        return name;
    }

    private int getCurrencyConversationOut() {
        Scanner sc = new Scanner(System.in);
        System.out.println("Select currency of out: ");
        System.out.println("1: USD" + "\n" + "2: EUR" + "\n" + "3: UAH");
        String sCurrencyOut = sc.nextLine();
        int currencyOut = Integer.parseInt(sCurrencyOut);
        return currencyOut;
    }

    private int getCurrencyConversationIn() {
        Scanner sc = new Scanner(System.in);
        System.out.println("Select currency for in: ");
        System.out.println("1: USD" + "\n" + "2: EUR" + "\n" + "3: UAH");
        String sCurrencyIn = sc.nextLine();
        int currencyIn = Integer.parseInt(sCurrencyIn);
        return currencyIn;
    }

    private synchronized void currencyConversion() { //конвертация валют
        CurrencyExchange currencyExchange = new CurrencyExchange();
        int name = getUserForConversation();
        int currencyOut = getCurrencyConversationOut();
        int currencyIn = getCurrencyConversationIn();
        double amount = getAmount();

        if (name == 1 || name == 2 || name == 3 || name == 4) {
            for (int i = 0; i < NAMES.length; i += 1) {
                for (int j = 0; j < CURRENCYS.length; j += 1) {

                    Query query = em.createNamedQuery("MoneyAccount.findAll", MoneyAccount.class);
                    List<MoneyAccount> m = query.getResultList();

                    for (MoneyAccount f : m) {
                        if (f.getNameOfUser().equals(NAMES[name - 1])) {
                            if (f.getCashInEUR() != 0 || f.getCashInUAH() != 0 || f.getCashInUSD() != 0) { //проверка на наличие денег
                                if (currencyOut == 1 && currencyIn == 3) { //USD in UAH conversion
                                    f.setCashInUAH(f.getCashInUAH() + (amount * currencyExchange.getUahForBuyinUSD()));
                                    f.setCashInUSD(f.getCashInUSD() - amount);
                                    mergeMoneyAccount(f);
                                    Transaction transaction = new Transaction(NAMES[name - 1], amount, CURRENCYS[currencyOut - 1], amount * currencyExchange.getUahForBuyinUSD(), CURRENCYS[currencyIn - 1], new Date(),
                                            amount, CURRENCYS[currencyOut - 1], CURRENCYS[currencyIn - 1], amount * currencyExchange.getUahForBuyinUSD());
                                    saveTransaction(transaction);
                                } else if (currencyOut == 2 && currencyIn == 3) { //EUR in UAH conversion
                                    f.setCashInUAH(f.getCashInUAH() + (amount * currencyExchange.getUahForBuyEUR()));
                                    f.setCashInEUR(f.getCashInEUR() - amount);
                                    mergeMoneyAccount(f);
                                    Transaction transaction = new Transaction(NAMES[name - 1], amount, CURRENCYS[currencyOut - 1], amount * currencyExchange.getUahForBuyEUR(), CURRENCYS[currencyIn - 1], new Date(),
                                            amount, CURRENCYS[currencyOut - 1], CURRENCYS[currencyIn - 1], amount * currencyExchange.getUahForBuyEUR());
                                    saveTransaction(transaction);
                                } else if (currencyOut == 3 && currencyIn == 1) { //UAH in USD conversion
                                    f.setCashInUSD(f.getCashInUSD() + (Precision.round(amount / currencyExchange.getUsdForSaleinUAH(), 2)));
                                    f.setCashInUAH(f.getCashInUAH() - amount);
                                    mergeMoneyAccount(f);
                                    Transaction transaction = new Transaction(NAMES[name - 1], amount, CURRENCYS[currencyOut - 1], Precision.round(amount / currencyExchange.getUsdForSaleinUAH(), 2), CURRENCYS[currencyIn - 1], new Date(),
                                            amount, CURRENCYS[currencyOut - 1], CURRENCYS[currencyIn - 1], Precision.round(amount / currencyExchange.getUsdForSaleinUAH(), 2));
                                    saveTransaction(transaction);
                                } else if (currencyOut == 3 && currencyIn == 2) { //UAH in EUR conversion
                                    f.setCashInEUR(f.getCashInEUR() + (Precision.round(amount / currencyExchange.getEurForSaleinUAH(), 2)));
                                    f.setCashInUAH(f.getCashInUAH() - amount);
                                    mergeMoneyAccount(f);
                                    Transaction transaction = new Transaction(NAMES[name - 1], amount, CURRENCYS[currencyOut - 1], Precision.round(amount / currencyExchange.getEurForSaleinUAH(), 2), CURRENCYS[currencyIn - 1], new Date(),
                                            amount, CURRENCYS[currencyOut - 1], CURRENCYS[currencyIn - 1], Precision.round(amount / currencyExchange.getEurForSaleinUAH(), 2));
                                    saveTransaction(transaction);
                                } else if (currencyOut == 1 && currencyIn == 2) { //USD in EUR conversion
                                    f.setCashInEUR(f.getCashInEUR() + (Precision.round(amount * currencyExchange.getUsdForBuyinEUR(), 2)));
                                    f.setCashInUSD(f.getCashInUSD() - amount);
                                    mergeMoneyAccount(f);
                                    Transaction transaction = new Transaction(NAMES[name - 1], amount, CURRENCYS[currencyOut - 1], Precision.round(amount / currencyExchange.getUsdForBuyinEUR(), 2), CURRENCYS[currencyIn - 1], new Date(),
                                            amount, CURRENCYS[currencyOut - 1], CURRENCYS[currencyIn - 1], Precision.round(amount / currencyExchange.getUsdForBuyinEUR(), 2));
                                    saveTransaction(transaction);
                                } else if (currencyOut == 2 && currencyIn == 1) { //EUR in USD conversion
                                    f.setCashInUSD(f.getCashInUSD() + (Precision.round(amount * currencyExchange.getEurForBuyinUSD(), 2)));
                                    f.setCashInEUR(f.getCashInEUR() - amount);
                                    mergeMoneyAccount(f);
                                    Transaction transaction = new Transaction(NAMES[name - 1], amount, CURRENCYS[currencyOut - 1], Precision.round(amount / currencyExchange.getEurForBuyinUSD(), 2), CURRENCYS[currencyIn - 1], new Date(),
                                            amount, CURRENCYS[currencyOut - 1], CURRENCYS[currencyIn - 1], Precision.round(amount / currencyExchange.getEurForBuyinUSD(), 2));
                                    saveTransaction(transaction);
                                }
                            } else {
                                System.out.println("User not have money!");
                                continue;
                            }
                        }
                    }
                    break;
                }
                break;
            }
        }
    }

    private void getCash(Scanner sc) { //получаю общую сумму денег пользователя в UAH
        System.out.println("Select user for check money: ");
        System.out.println("1: " + NAMES[0] + "\n" + "2: " + NAMES[1] + "\n" + "3: " + NAMES[2] + "\n" + "4: " + NAMES[3]);
        String sName = sc.nextLine();
        int name = Integer.parseInt(sName);
        double cashInUAH = 0;
        CurrencyExchange currencyExchange = new CurrencyExchange();

        try {
            if (name == 1 || name == 2 || name == 3 || name == 4) {
                for (int i = 0; i < NAMES.length; i += 1) {
                    for (int j = 0; j < CURRENCYS.length; j += 1) {

                        Query query = em.createNamedQuery("MoneyAccount.findAll", MoneyAccount.class);
                        List<MoneyAccount> m = query.getResultList();

                        for (MoneyAccount f : m) {
                            if (f.getNameOfUser().equals(NAMES[name - 1])) {
                                cashInUAH = f.getCashInUAH() + (f.getCashInUSD() * currencyExchange.getUsdForSaleinUAH()) + (f.getCashInEUR() * currencyExchange.getEurForSaleinUAH());
                            }
                        }
                    }
                }
            }
            System.out.println("Total money in UAH at user " + NAMES[name - 1] + ": " + cashInUAH);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createCurrencyExchange() { //создаю таблицу валют
        CurrencyExchange currencyExchange = new CurrencyExchange();
        em.getTransaction().begin();
        try {
            em.persist(currencyExchange);
            em.getTransaction().commit();
        } catch (Exception e) {
            e.printStackTrace();
            em.getTransaction().rollback();
        }
    }

    private void createMoneyAccount() { //создаю счета пользователей
        em.getTransaction().begin();
        try {
            for (int i = 0; i < NAMES.length; i += 1) {
                MoneyAccount moneyAccount = new MoneyAccount();
                moneyAccount.setNameOfUser(NAMES[i]);
                em.persist(moneyAccount);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            e.printStackTrace();
            em.getTransaction().rollback();
        }
    }

    private void createUsers() { //создаю пользователей
        em.getTransaction().begin();
        try {
            for (int i = 0; i < NAMES.length; i += 1) {
                User user = new User(NAMES[i]);
                em.persist(user);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            e.printStackTrace();
            em.getTransaction().rollback();
        }
    }

    private Transaction saveTransaction(Transaction transaction) {
        em.getTransaction().begin();
        try {
            em.persist(transaction);
            em.getTransaction().commit();
        } catch (Exception e) {
            e.printStackTrace();
            em.getTransaction().rollback();
        }
        return transaction;
    }

    private MoneyAccount mergeMoneyAccount(MoneyAccount moneyAccount) {
        Query query = em.createQuery("select c from MoneyAccount c", MoneyAccount.class);
        List<MoneyAccount> moneyAccounts = query.getResultList();
        em.getTransaction().begin();
        try {
            for (MoneyAccount f : moneyAccounts) {
                em.merge(f);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            e.printStackTrace();
            em.getTransaction().rollback();
        }
        return moneyAccount;
    }

    private final String[] NAMES = {"Ivan", "Nikolai", "Olia", "Viktoria"};
    private final String[] CURRENCYS = {"USD", "EUR", "UAH"};

}
