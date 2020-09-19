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
                            toUpAmount(sc);
                            break;
                        case "3":
                            moneyTransfer(sc);
                            break;
                        case "4":
                            currencyConversion(sc);
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

    private void toUpAmount(Scanner sc) { //пополняю счет // заранее создал готовых пользователей и готовые названия валют, в самом низу класса
        System.out.println("Select user: ");
        System.out.println("1: " + NAMES[0] + "\n" + "2: " + NAMES[1] + "\n" + "3: " + NAMES[2] + "\n" + "4: " + NAMES[3]);
        String sName = sc.nextLine();
        int name = Integer.parseInt(sName);

        System.out.println("Select currency: ");
        System.out.println("1: USD" + "\n" + "2: EUR" + "\n" + "3: UAH");
        String sCurrency = sc.nextLine();
        int currency = Integer.parseInt(sCurrency);

        System.out.print("Enter the amount of money: ");
        String sAmount = sc.nextLine();
        double amount = Double.valueOf(sAmount);

        em.getTransaction().begin();
        try {
            if (name == 1 || name == 2 || name == 3 || name == 4) {
                for (int i = 0; i < NAMES.length; i += 1) {
                    for (int j = 0; j < CURRENCYS.length; j += 1) {
                        Transaction transaction = new Transaction(NAMES[name - 1], 0, "none", amount,
                                CURRENCYS[currency - 1], new Date(), 0, "none", "none", 0);

                        Query query = em.createNamedQuery("MoneyAccount.findAll", MoneyAccount.class);
                        List<MoneyAccount> m = query.getResultList();

                        for (MoneyAccount f : m) {
                            if (f.getNameOfUser().equals(NAMES[name - 1])) {
                                if (currency == 1) {
                                    f.setCashInUSD(f.getCashInUSD() + amount);
                                    em.merge(f);                                    //постоянно обновляю значения в таблице moneyaccounts
                                } else if (currency == 2) {
                                    f.setCashInEUR(f.getCashInEUR() + amount);
                                    em.merge(f);
                                } else if (currency == 3) {
                                    f.setCashInUAH(f.getCashInUAH() + amount);
                                    em.merge(f);
                                }
                            }
                        }
                        em.persist(transaction);
                        break;
                    }
                    break;
                }
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            e.printStackTrace();
            em.getTransaction().rollback();
        }
    }

    private synchronized void moneyTransfer(Scanner sc) { //перевод денег
        System.out.println("Select user for money out: ");
        System.out.println("1: " + NAMES[0] + "\n" + "2: " + NAMES[1] + "\n" + "3: " + NAMES[2] + "\n" + "4: " + NAMES[3]);
        String sNameOut = sc.nextLine();
        int nameOut = Integer.parseInt(sNameOut);

        System.out.println("Select currency: ");
        System.out.println("1: USD" + "\n" + "2: EUR" + "\n" + "3: UAH");
        String sCurrency = sc.nextLine();
        int currency = Integer.parseInt(sCurrency);

        System.out.print("Enter the amount of money: ");
        String sAmount = sc.nextLine();
        double amount = Double.valueOf(sAmount);

        System.out.println("Select user for money take: ");
        System.out.println("1: " + NAMES[0] + "\n" + "2: " + NAMES[1] + "\n" + "3: " + NAMES[2] + "\n" + "4: " + NAMES[3]);
        String sNameTake = sc.nextLine();
        int nameTake = Integer.parseInt(sNameTake);

        em.getTransaction().begin();
        try {
            if (nameOut == 1 || nameOut == 2 || nameOut == 3 || nameOut == 4 &&
                    nameTake == 1 || nameTake == 2 || nameTake == 3 || nameTake == 4) {
                for (int i = 0; i < NAMES.length; i += 1) {
                    for (int j = 0; j < CURRENCYS.length; j += 1) {

                        Query query = em.createNamedQuery("MoneyAccount.findAll", MoneyAccount.class);
                        List<MoneyAccount> m = query.getResultList();

                        for (MoneyAccount f : m) {
                            if (NAMES[nameOut - 1].equals(NAMES[nameTake - 1])) continue; //если пытается сам себе перевести, то пусть попытается еще раз
                            if (!NAMES[nameOut - 1].equals(NAMES[nameTake - 1]) && f.getNameOfUser().equals(NAMES[nameOut - 1]) &&
                                    f.getCashInEUR() >= amount || f.getCashInUAH() >= amount || f.getCashInUSD() >= amount) { //проверка на наличие денег и чтобы пользователь сам себе не пыталься переводить деньги
                                if (currency == 1) {
                                    f.setCashInUSD(f.getCashInUSD() - amount);
                                    em.merge(f);
                                } else if (currency == 2) {
                                    f.setCashInEUR(f.getCashInEUR() - amount);
                                    em.merge(f);
                                } else if (currency == 3) {
                                    f.setCashInUAH(f.getCashInUAH() - amount);
                                    em.merge(f);
                                }
                                Transaction transactionOut = new Transaction(NAMES[nameOut - 1], amount, CURRENCYS[currency - 1],
                                        0, "none", new Date(), 0, "none", "none", 0);
                                em.persist(transactionOut);

                            } else if (!NAMES[nameOut - 1].equals(NAMES[nameTake - 1]) && f.getNameOfUser().equals(NAMES[nameTake - 1]) &&
                                    (f.getNameOfUser().equals(NAMES[nameOut - 1]) && f.getCashInEUR() >= amount || f.getCashInUAH() >= amount || f.getCashInUSD() >= amount)) {
                                if (currency == 1) {
                                    f.setCashInUSD(f.getCashInUSD() + amount);
                                    em.merge(f);
                                } else if (currency == 2) {
                                    f.setCashInEUR(f.getCashInEUR() + amount);
                                    em.merge(f);
                                } else if (currency == 3) {
                                    f.setCashInUAH(f.getCashInUAH() + amount);
                                    em.merge(f);
                                }
                                Transaction transactionTake = new Transaction(NAMES[nameTake - 1], 0, "none", amount,
                                        CURRENCYS[currency - 1], new Date(), 0, "none", "none", 0);
                                em.persist(transactionTake);
                            } else {
                                System.out.println("You do not have enough money to transfer");
                                break;
                            }
                        }
                        break;
                    }
                    break;
                }
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            e.printStackTrace();
            em.getTransaction().rollback();
        }
    }

    private synchronized void currencyConversion(Scanner sc) { //конвертация валют
        System.out.println("Select user for conversion: ");
        System.out.println("1: " + NAMES[0] + "\n" + "2: " + NAMES[1] + "\n" + "3: " + NAMES[2] + "\n" + "4: " + NAMES[3]);
        String sName = sc.nextLine();
        int name = Integer.parseInt(sName);

        System.out.println("Select currency of out: ");
        System.out.println("1: USD" + "\n" + "2: EUR" + "\n" + "3: UAH");
        String sCurrencyOut = sc.nextLine();
        int currencyOut = Integer.parseInt(sCurrencyOut);

        System.out.println("Select currency for in: ");
        System.out.println("1: USD" + "\n" + "2: EUR" + "\n" + "3: UAH");
        String sCurrencyIn = sc.nextLine();
        int currencyIn = Integer.parseInt(sCurrencyIn);

        System.out.println("Enter the amount of money for conversion: ");
        String sAmount = sc.nextLine();
        Double amount = Double.valueOf(sAmount);
        CurrencyExchange currencyExchange = new CurrencyExchange();

        em.getTransaction().begin();
        try {
            if (name == 1 || name == 2 || name == 3 || name == 4) {
                for (int i = 0; i < NAMES.length; i += 1) {
                    for (int j = 0; j < CURRENCYS.length; j += 1) {

                        Query query = em.createNamedQuery("MoneyAccount.findAll", MoneyAccount.class);
                        List<MoneyAccount> m = query.getResultList();

                        for (MoneyAccount f : m) {
                            if (f.getNameOfUser().equals(NAMES[name - 1])) {
                                if (f.getCashInEUR() != 0 || f.getCashInUAH() != 0 || f.getCashInUSD() != 0) {
                                    if (currencyOut == 1 && currencyIn == 3) { //USD in UAH conversion
                                        f.setCashInUAH(f.getCashInUAH() + (amount * currencyExchange.getUahForBuyinUSD()));
                                        f.setCashInUSD(f.getCashInUSD() - amount);
                                        em.merge(f);
                                        Transaction transaction = new Transaction(NAMES[name - 1], amount, CURRENCYS[currencyOut - 1], amount * currencyExchange.getUahForBuyinUSD(), CURRENCYS[currencyIn - 1], new Date(),
                                                amount, CURRENCYS[currencyOut - 1], CURRENCYS[currencyIn - 1], amount * currencyExchange.getUahForBuyinUSD());
                                        em.persist(transaction);
                                    } else if (currencyOut == 2 && currencyIn == 3) { //EUR in UAH conversion
                                        f.setCashInUAH(f.getCashInUAH() + (amount * currencyExchange.getUahForBuyEUR()));
                                        f.setCashInEUR(f.getCashInEUR() - amount);
                                        em.merge(f);
                                        Transaction transaction = new Transaction(NAMES[name - 1], amount, CURRENCYS[currencyOut - 1], amount * currencyExchange.getUahForBuyEUR(), CURRENCYS[currencyIn - 1], new Date(),
                                                amount, CURRENCYS[currencyOut - 1], CURRENCYS[currencyIn - 1], amount * currencyExchange.getUahForBuyEUR());
                                        em.persist(transaction);
                                    } else if (currencyOut == 3 && currencyIn == 1) { //UAH in USD conversion
                                        f.setCashInUSD(f.getCashInUSD() + (Precision.round(amount / currencyExchange.getUsdForSaleinUAH(), 2)));
                                        f.setCashInUAH(f.getCashInUAH() - amount);
                                        em.merge(f);
                                        Transaction transaction = new Transaction(NAMES[name - 1], amount, CURRENCYS[currencyOut - 1], Precision.round(amount / currencyExchange.getUsdForSaleinUAH(), 2), CURRENCYS[currencyIn - 1], new Date(),
                                                amount, CURRENCYS[currencyOut - 1], CURRENCYS[currencyIn - 1], Precision.round(amount / currencyExchange.getUsdForSaleinUAH(), 2));
                                        em.persist(transaction);
                                    } else if (currencyOut == 3 && currencyIn == 2) { //UAH in EUR conversion
                                        f.setCashInEUR(f.getCashInEUR() + (Precision.round(amount / currencyExchange.getEurForSaleinUAH(), 2)));
                                        f.setCashInUAH(f.getCashInUAH() - amount);
                                        em.merge(f);
                                        Transaction transaction = new Transaction(NAMES[name - 1], amount, CURRENCYS[currencyOut - 1], Precision.round(amount / currencyExchange.getEurForSaleinUAH(), 2), CURRENCYS[currencyIn - 1], new Date(),
                                                amount, CURRENCYS[currencyOut - 1], CURRENCYS[currencyIn - 1], Precision.round(amount / currencyExchange.getEurForSaleinUAH(), 2));
                                        em.persist(transaction);
                                    } else if (currencyOut == 1 && currencyIn == 2) { //USD in EUR conversion
                                        f.setCashInEUR(f.getCashInEUR() + (Precision.round(amount * currencyExchange.getUsdForBuyinEUR(), 2)));
                                        f.setCashInUSD(f.getCashInUSD() - amount);
                                        em.merge(f);
                                        Transaction transaction = new Transaction(NAMES[name - 1], amount, CURRENCYS[currencyOut - 1], Precision.round(amount / currencyExchange.getUsdForBuyinEUR(), 2), CURRENCYS[currencyIn - 1], new Date(),
                                                amount, CURRENCYS[currencyOut - 1], CURRENCYS[currencyIn - 1], Precision.round(amount / currencyExchange.getUsdForBuyinEUR(), 2));
                                        em.persist(transaction);
                                    } else if (currencyOut == 2 && currencyIn == 1) { //EUR in USD conversion
                                        f.setCashInUSD(f.getCashInUSD() + (Precision.round(amount * currencyExchange.getEurForBuyinUSD(), 2)));
                                        f.setCashInEUR(f.getCashInEUR() - amount);
                                        em.merge(f);
                                        Transaction transaction = new Transaction(NAMES[name - 1], amount, CURRENCYS[currencyOut - 1], Precision.round(amount / currencyExchange.getEurForBuyinUSD(), 2), CURRENCYS[currencyIn - 1], new Date(),
                                                amount, CURRENCYS[currencyOut - 1], CURRENCYS[currencyIn - 1], Precision.round(amount / currencyExchange.getEurForBuyinUSD(), 2));
                                        em.persist(transaction);
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
            em.getTransaction().commit();
        } catch (Exception e) {
            e.printStackTrace();
            em.getTransaction().rollback();
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

    private final String[] NAMES = {"Ivan", "Nikolai", "Olia", "Viktoria"};
    private final String[] CURRENCYS = {"USD", "EUR", "UAH"};

}
