import java.io.*;
import java.util.*;

public class BankingApp {

    static class User {
        String name, email, pin, accountType, accountNo;

        User(String n, String e, String p, String t, String a) {
            name = n;
            email = e;
            pin = p;
            accountType = t;
            accountNo = a;
        }
    }

    static class Account {
        String accountNo;
        double balance;

        Account(String a, double b) {
            accountNo = a;
            balance = b;
        }

        void deposit(double amount) {
            balance += amount;
        }

        boolean withdraw(double amount) {
            if (amount <= balance) {
                balance -= amount;
                return true;
            }
            return false;
        }
    }

    static class LoanRequest {
        String name, accountNo, loanType, status;
        double amount;

        LoanRequest(String name, String accountNo, String loanType, double amount, String status) {
            this.name = name;
            this.accountNo = accountNo;
            this.loanType = loanType;
            this.amount = amount;
            this.status = status;
        }
    }

    static class VaultService {
        String vaultOtp;

        void generateOtp() {
            vaultOtp = String.format("%04d", new Random().nextInt(10000));
            System.out.println("Your Vault OTP is: " + vaultOtp);
        }

        boolean verifyOtp(String enteredOtp) {
            return vaultOtp != null && vaultOtp.equals(enteredOtp);
        }
    }

    static class UndoService {
        MyStack undoStack = new MyStack();
        MyStack amountStack = new MyStack();
        MyStack typeStack = new MyStack();

        void recordUndo(String operation, double amount, String type) {
            undoStack.push(operation.hashCode());
            amountStack.push((int) amount);
            typeStack.push(type.hashCode());
        }

        void undoLast(Account account) {
            if (!undoStack.isEmpty() && !amountStack.isEmpty() && !typeStack.isEmpty()) {
                undoStack.pop();
                double amount = amountStack.pop();
                int typeHash = typeStack.pop();
                String type = (typeHash == "deposit".hashCode()) ? "deposit" : "withdraw";

                if (type.equals("deposit")) {
                    account.withdraw(amount);
                    System.out.println("Undid deposit of: " + amount);
                } else if (type.equals("withdraw")) {
                    account.deposit(amount);
                    System.out.println("Undid withdrawal of: " + amount);
                }
            } else {
                System.out.println("No operation to undo.");
            }
        }
    }

    static class BSTree {
        class Node {
            Account data;
            Node left, right;

            Node(Account data) {
                this.data = data;
            }
        }

        Node root = null;

        void insert(Account account) {
            root = insertRec(root, account);
        }

        Node insertRec(Node node, Account account) {
            if (node == null) return new Node(account);

            int cmp = account.accountNo.compareTo(node.data.accountNo);
            if (cmp < 0)
                node.left = insertRec(node.left, account);
            else if
            (cmp > 0) node.right = insertRec(node.right, account);
            return node;
        }

        Account search(String accountNo) {
            Node current = root;
            while (current != null) {
                int cmp = accountNo.compareTo(current.data.accountNo);
                if (cmp == 0) return current.data;
                else if (cmp < 0) current = current.left;
                else current = current.right;
            }
            return null;
        }
    }

    static class MyStack {
        class Node {
            int value;
            Node next;
            Node(int v) { value = v; }
        }

        Node top;

        void push(int val) {
            Node node = new Node(val);
            node.next = top;
            top = node;
        }

        int pop() {
            if (top == null) return -1;
            int v = top.value;
            top = top.next;
            return v;
        }

        boolean isEmpty() {
            return top == null;
        }
    }

    static class MyQueue {
        class Node {
            LoanRequest value;
            Node next;
            Node(LoanRequest v) { value = v; }
        }

        Node front, rear;

        void enqueue(LoanRequest val) {
            Node node = new Node(val);
            if (rear == null) front = rear = node;
            else {
                rear.next = node;
                rear = node;
            }
        }

        LoanRequest dequeue() {
            if (front == null) return null;
            LoanRequest val = front.value;
            front = front.next;
            if (front == null) rear = null;
            return val;
        }

        boolean isEmpty() {
            return front == null;
        }
    }

    static class AuthService {
        final String USER_F = "users.txt", ACCT_F = "accounts.txt";
        BSTree tree = new BSTree();
        Map<String, String> creds = new HashMap<>();

        AuthService() {
            load();
        }

        void load() {
            try (BufferedReader u = new BufferedReader(new FileReader(USER_F))) {
                String l;
                while ((l = u.readLine()) != null) {
                    String[] a = l.split(",");
                    creds.put(a[1], a[2]);
                    tree.insert(new Account(a[4], 0));
                }
            } catch (Exception e) {}
            try (BufferedReader a = new BufferedReader(new FileReader(ACCT_F))) {
                String l;
                while ((l = a.readLine()) != null) {
                    String[] f = l.split(",");
                    Account ac = tree.search(f[0]);
                    if (ac != null) ac.balance = Double.parseDouble(f[1]);
                }
            } catch (Exception e) {}
        }

        void saveBalance(Account ac) {
            try {
                List<String> lines = new ArrayList<>();
                BufferedReader reader = new BufferedReader(new FileReader(ACCT_F));
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(",");
                    if (parts[0].equals(ac.accountNo)) {
                        lines.add(ac.accountNo + "," + ac.balance);
                    } else {
                        lines.add(line);
                    }
                }
                reader.close();
                PrintWriter writer = new PrintWriter(new FileWriter(ACCT_F));
                for (String l : lines) writer.println(l);
                writer.close();
            } catch (Exception e) {
                System.out.println("Error updating balance.");
            }
        }

        User register(Scanner sc) throws IOException {
            System.out.print("Name: ");
            String n = sc.nextLine();
            System.out.print("Email: ");
            String e = sc.nextLine();
            System.out.print("PIN: ");
            String p = sc.nextLine();
            System.out.print("Type (SAV/CUR): ");
            String t = sc.nextLine();
            String aNo = String.valueOf(Math.abs(new Random().nextLong())).substring(0, 10);

            try (PrintWriter pw = new PrintWriter(new FileWriter(USER_F, true))) {
                pw.printf("%s,%s,%s,%s,%s\n", n, e, p, t, aNo);
            }
            try (PrintWriter pw = new PrintWriter(new FileWriter(ACCT_F, true))) {
                pw.printf("%s,0.0\n", aNo);
            }

            creds.put(e, p);
            Account ac = new Account(aNo, 0);
            tree.insert(ac);
            System.out.println("Registered! Acc#: " + aNo);
            return new User(n, e, p, t, aNo);
        }

        User login(Scanner sc) {
            System.out.print("Email: ");
            String e = sc.nextLine();
            System.out.print("PIN: ");
            String p = sc.nextLine();
            if (creds.containsKey(e) && creds.get(e).equals(p)) {
                try (BufferedReader br = new BufferedReader(new FileReader(USER_F))) {
                    String l;
                    while ((l = br.readLine()) != null) {
                        String[] a = l.split(",");
                        if (a[1].equals(e)) return new User(a[0], e, p, a[3], a[4]);
                    }
                } catch (Exception ex) {}
            }
            System.out.println("Bad credentials.");
            return null;
        }
    }

    static class LoanService {
        final String LN_F = "loans.txt";
        MyQueue q = new MyQueue();

        void apply(User user, Account account, Scanner sc) throws IOException {
            System.out.print("Loan Type (Car, Home, Education, Business): ");
            String t = sc.nextLine();
            System.out.print("Amount: ");
            double am = Double.parseDouble(sc.nextLine());

            if (am > 500000) {
                System.out.println("Loan not applied. Amount exceeds ₹500000 limit.");
                return;
            }

            String status = "APPROVED";
            LoanRequest lr = new LoanRequest(user.name, account.accountNo, t, am, status);
            try (PrintWriter pw = new PrintWriter(new FileWriter(LN_F, true))) {
                pw.printf("%s,%s,%s,%.2f,%s\n", lr.name, lr.accountNo, lr.loanType, lr.amount, lr.status);
            }
            q.enqueue(lr);
            System.out.println("Loan applied successfully.");
        }

        void viewLoanStatus(String accountNo) throws IOException {
            boolean found = false;
            try (BufferedReader br = new BufferedReader(new FileReader(LN_F))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] p = line.split(",");
                    if (p.length >= 5 && p[1].equals(accountNo)) {
                        found = true;
                        System.out.println("Loan Type: " + p[2]);
                        System.out.println("Amount: " + p[3]);
                        System.out.println("Status: " + p[4]);
                        break;
                    }
                }
            }
            if (!found) System.out.println("No loan found for this account.");
        }
    }

    public static void main(String[] args) throws Exception {
        Scanner sc = new Scanner(System.in);
        AuthService auth = new AuthService();
        System.out.println("SMART VAULT");
        System.out.println("1. Register  2. Login");
        String c = sc.nextLine();
        User user = c.equals("1") ? auth.register(sc) : auth.login(sc);
        if (user == null) return;

        Account acct = auth.tree.search(user.accountNo);
        LoanService ln = new LoanService();
        VaultService vault = new VaultService();
        UndoService undo = new UndoService();

        while (true) {
            System.out.println("\n1. Deposit 2. Withdraw 3. Balance 4. Loan 5. Loan Status 6. Vault 7. Undo 8. Exit");
            switch (sc.nextLine()) {
                case "1":
                    System.out.print("Amt: ");
                    double d = Double.parseDouble(sc.nextLine());
                    acct.deposit(d);
                    undo.recordUndo("Deposit", d, "deposit");
                    auth.saveBalance(acct);
                    System.out.println("Deposited: " + d);
                    break;
                case "2":
                    System.out.print("Amt: ");
                    double w = Double.parseDouble(sc.nextLine());
                    if (acct.withdraw(w)) {
                        undo.recordUndo("Withdraw", w, "withdraw");
                        auth.saveBalance(acct);
                        System.out.println("Withdrawn: " + w);
                    } else System.out.println("Insufficient balance.");
                    break;
                case "3":
                    System.out.println("Balance: ₹" + acct.balance);
                    break;
                case "4":
                    ln.apply(user, acct, sc);
                    break;
                case "5":


                    System.out.print("Enter Account No to check loan status: ");
                    String accountNo = sc.nextLine();
                    ln.viewLoanStatus(accountNo);


                break;
                case "6":
                    vault.generateOtp();
                    System.out.print("Enter OTP to access vault: ");
                    String otp = sc.nextLine();
                    if (vault.verifyOtp(otp)) {
                        System.out.print("Enter amount to deposit: ");
                        double amt = Double.parseDouble(sc.nextLine());
                        acct.deposit(amt);
                        undo.recordUndo("Deposit", amt, "deposit");
                        auth.saveBalance(acct);
                        System.out.println("Amount added: " + amt);
                    } else System.out.println("Invalid OTP.");
                    break;
                case "7":
                    undo.undoLast(acct);
                    auth.saveBalance(acct);
                    break;
                case "8":
                    System.out.println("Bye.");
                    return;
            }
        }
    }
}
