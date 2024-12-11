import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.HashMap;
import javax.swing.*;

public class b1 extends JFrame implements ActionListener {
    private JPanel cards;
    private CardLayout cardLayout;
    private JTextField userField, passField, newUserField, newPassField, amountField;
    private JTextArea resultArea;
    private JButton loginButton, createUserButton, goToCreateUserButton, goToLoginButton, depositButton, withdrawButton, viewBalanceButton, signOutButton;
    private HashMap<String, String> userDatabase = new HashMap<>();
    private HashMap<String, Double> userBalances = new HashMap<>();
    private String currentUser = null;
    private double balance = 0.0;

    public b1() {
        setTitle("Banking Application");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 400);
        setLayout(new BorderLayout());
        loadUserData();

        cardLayout = new CardLayout();
        cards = new JPanel(cardLayout);

        cards.add(createLoginPage(), "Login");
        cards.add(createActionPage(), "Actions");
        cards.add(createUserPage(), "CreateUser");

        add(cards, BorderLayout.CENTER);
        cardLayout.show(cards, "Login");

        Runtime.getRuntime().addShutdownHook(new Thread(this::saveUserData));
        setVisible(true);
    }

    private JPanel createLoginPage() {
        JPanel panel = createPanel("Login", new Color(173, 216, 230));
        userField = new JTextField(15);
        passField = new JPasswordField(15);

        panel.add(new JLabel("Username:"), createConstraints(0, 0));
        panel.add(userField, createConstraints(1, 0));
        panel.add(new JLabel("Password:"), createConstraints(0, 1));
        panel.add(passField, createConstraints(1, 1));

        loginButton = new JButton("Login");
        loginButton.addActionListener(this);
        goToCreateUserButton = new JButton("Create Account");
        goToCreateUserButton.addActionListener(this);

        panel.add(loginButton, createConstraints(0, 2, 2, 1, GridBagConstraints.CENTER));
        panel.add(goToCreateUserButton, createConstraints(0, 3, 2, 1, GridBagConstraints.CENTER));

        return panel;
    }
    private JPanel createActionPage() {
        JPanel panel = createPanel("Actions", new Color(240, 255, 240));
    
        JPanel buttonPanel = new JPanel(new GridLayout(3, 2, 10, 10));  
        JLabel amountLabel = new JLabel("Amount:");
        amountField = new JTextField(15);
    
        depositButton = new JButton("Deposit");
        withdrawButton = new JButton("Withdraw");
        viewBalanceButton = new JButton("View Balance");
        signOutButton = new JButton("Sign Out");
    
        depositButton.addActionListener(this);
        withdrawButton.addActionListener(this);
        viewBalanceButton.addActionListener(this);
        signOutButton.addActionListener(this);
    
        buttonPanel.add(amountLabel);
        buttonPanel.add(amountField);
        buttonPanel.add(depositButton);
        buttonPanel.add(withdrawButton);
        buttonPanel.add(viewBalanceButton);
    
        resultArea = new JTextArea(10, 50);
        resultArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(resultArea);
    
        panel.setLayout(new BorderLayout());  
        panel.add(buttonPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(signOutButton, BorderLayout.SOUTH);
    
        return panel;
    }
    
    private JPanel createUserPage() {
        JPanel panel = createPanel("Create Account", new Color(255, 228, 225));
        newUserField = new JTextField(15);
        newPassField = new JPasswordField(15);

        panel.add(new JLabel("New Username:"), createConstraints(0, 0));
        panel.add(newUserField, createConstraints(1, 0));
        panel.add(new JLabel("New Password:"), createConstraints(0, 1));
        panel.add(newPassField, createConstraints(1, 1));

        createUserButton = new JButton("Create Account");
        createUserButton.addActionListener(this);
        goToLoginButton = new JButton("Existing User Login");
        goToLoginButton.addActionListener(this);

        panel.add(createUserButton, createConstraints(0, 2, 2, 1, GridBagConstraints.CENTER));
        panel.add(goToLoginButton, createConstraints(0, 3, 2, 1, GridBagConstraints.CENTER));

        return panel;
    }

    private JPanel createPanel(String title, Color bgColor) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder(title));
        panel.setBackground(bgColor);
        return panel;
    }

    private GridBagConstraints createConstraints(int x, int y) {
        return createConstraints(x, y, 1, 1, GridBagConstraints.WEST);
    }

    private GridBagConstraints createConstraints(int x, int y, int width, int height, int anchor) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = width;
        gbc.gridheight = height;
        gbc.anchor = anchor;
        return gbc;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source == loginButton) handleLogin();
        else if (source == goToCreateUserButton) cardLayout.show(cards, "CreateUser");
        else if (source == goToLoginButton) cardLayout.show(cards, "Login");
        else if (source == createUserButton) handleCreateUser();
        else if (source == depositButton) handleTransaction(true);
        else if (source == withdrawButton) handleTransaction(false);
        else if (source == viewBalanceButton) resultArea.append("Current balance: $" + balance + "\n");
        else if (source == signOutButton) handleSignOut();
    }

    private void handleLogin() {
        String username = userField.getText().trim();
        String password = passField.getText().trim();
        if (userDatabase.containsKey(username) && userDatabase.get(username).equals(password)) {
            currentUser = username;
            balance = userBalances.getOrDefault(username, 0.0);
            resultArea.setText("Welcome, " + username + "!\n");
            cardLayout.show(cards, "Actions");
        } else {
            JOptionPane.showMessageDialog(this, "Invalid username or password.", "Login Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleCreateUser() {
        String newUsername = newUserField.getText().trim();
        String newPassword = newPassField.getText().trim();
        if (!newUsername.isEmpty() && !newPassword.isEmpty() && !userDatabase.containsKey(newUsername)) {
            userDatabase.put(newUsername, newPassword);
            userBalances.put(newUsername, 0.0);
            JOptionPane.showMessageDialog(this, "Account created successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            cardLayout.show(cards, "Login");
        } else {
            JOptionPane.showMessageDialog(this, "Username already exists or fields are empty.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleTransaction(boolean isDeposit) {
        try {
            double amount = Double.parseDouble(amountField.getText().trim());
            if (amount <= 0) throw new NumberFormatException();
            if (isDeposit) balance += amount;
            else if (amount > balance) resultArea.append("Insufficient funds.\n");
            else balance -= amount;
            resultArea.append((isDeposit ? "Deposited" : "Withdrew") + " $" + amount + ". New balance: $" + balance + "\n");
        } catch (NumberFormatException ex) {
            resultArea.append("Enter a valid numeric amount.\n");
        }
    }

    private void handleSignOut() {
        userBalances.put(currentUser, balance);
        currentUser = null;
        balance = 0.0;
        cardLayout.show(cards, "Login");
    }

    private void loadUserData() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("users.dat"))) {
            userDatabase = (HashMap<String, String>) ois.readObject();
            userBalances = (HashMap<String, Double>) ois.readObject();
        } catch (IOException | ClassNotFoundException ex) {
            userDatabase = new HashMap<>();
            userBalances = new HashMap<>();
        }
    }

    private void saveUserData() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("users.dat"))) {
            oos.writeObject(userDatabase);
            oos.writeObject(userBalances);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new b1();
    }
}
