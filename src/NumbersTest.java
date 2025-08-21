import javax.swing.*;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.text.NumberFormat;
import java.util.*;
import java.util.List;

public class NumbersTest {
    private static final JFrame frame = new JFrame("Numbers");
    private static final String PAGE_1 = "PAGE_1";
    private static final String PAGE_2 = "PAGE_2";
    private static final ArrayList<JComponent> allCards = new ArrayList<>();
    private static final JPanel cardsPanel = new JPanel(new GridBagLayout());
    private static final CardLayout cardLayout = new CardLayout();
    private static final Random random = new Random();
    private static final int MAX_VALUE = 1000;
    private static final int MIN_POINT_VALUE = 30;
    private static final JTextField numberField = new JTextField();
    private static List<Integer> numbers = new ArrayList<>();
    private static Runnable relayout;
    private static int insertedValue;
    private static JScrollPane scroll;
    private static JPanel cards;
    private static JPanel page1;
    private static JPanel page2;
    private static SortingTypeEnum sortingType = SortingTypeEnum.ASCENDING;

    enum SortingTypeEnum {
        ASCENDING, DESCENDING
    }

    public static void main(String[] args) {
        createPages();
    }

    private static void createPages() {
        SwingUtilities.invokeLater(() -> {
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            cards = new JPanel(cardLayout);
            page1 = createPage_1();
            page2 = createPage_2();
            cards.add(page1, PAGE_1);
            cards.add(page2, PAGE_2);

            frame.setContentPane(cards);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    private static JPanel createPage_1() {
        JPanel page1 = new JPanel();
        page1.setLayout(new BoxLayout(page1, BoxLayout.Y_AXIS));
        page1.setBorder(BorderFactory.createEmptyBorder(400, 500, 400, 500));

        JLabel label = new JLabel("How many numbers to display?");
        label.setAlignmentX(Component.CENTER_ALIGNMENT);

        numberField.setColumns(20);
        numberField.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton enterButton = new JButton("Enter");
        enterButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        page1.add(label);
        page1.add(Box.createVerticalStrut(8));
        page1.add(numberField);
        page1.add(Box.createVerticalStrut(8));
        page1.add(enterButton);

        enterButton.addActionListener(NumbersTest::enterButtonHandler);

        return page1;
    }

    private static void enterButtonHandler(ActionEvent e) {
        String trimmedString = numberField.getText().trim();
        try {
            int value = Integer.parseInt(trimmedString);
            if (value <= 0 || value > MAX_VALUE) {
                JOptionPane.showMessageDialog(frame, "Invalid number, can't be over " + MAX_VALUE, "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            insertedValue = value;
            createCards();
            cardLayout.show(cards, PAGE_2);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(frame, "Invalid number", "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            numberField.setText("");
        }
    }

    private static JPanel createPage_2() {
        JPanel page2 = new JPanel(new BorderLayout(12, 12));

        // Left column: Cards
        scroll = new JScrollPane(cardsPanel,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.getVerticalScrollBar().setUnitIncrement(24);
        page2.add(scroll, BorderLayout.CENTER);

        // Right column: Sort / Reset
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        JButton sortButton = new JButton("Sort");
        JButton resetButton = new JButton("Reset");

        sortButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        resetButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        rightPanel.add(Box.createVerticalGlue());
        rightPanel.add(sortButton);
        rightPanel.add(Box.createVerticalStrut(8));
        rightPanel.add(resetButton);
        rightPanel.add(Box.createVerticalGlue());
        page2.add(rightPanel, BorderLayout.EAST);

        relayout = () -> {
            int vgap = 8, hgap = 16;
            int viewportHeight = scroll.getViewport().getExtentSize().height;
            if (viewportHeight <= 0) viewportHeight = Math.max(200, scroll.getHeight() - 32);
            relayoutCards(allCards, viewportHeight, vgap, hgap);
            cardsPanel.revalidate();
        };

        sortButton.addActionListener(NumbersTest::sortCards);
        resetButton.addActionListener(NumbersTest::resetHandler);
        return page2;
    }

    private static void resetHandler(ActionEvent e) {
        allCards.clear();
        insertedValue = 0;
        numbers.clear();
        cardLayout.show(cards, PAGE_1);
        if (sortingType == SortingTypeEnum.DESCENDING) {
            sortingType = SortingTypeEnum.ASCENDING;
        }
    }

    private static void createCards() {
        allCards.clear();
        numbers.clear();
        boolean isThereMinPoint = false;
        for (int i = 1; i <= insertedValue; i++) {
            int num = random.nextInt(MAX_VALUE);
            if (num <= MIN_POINT_VALUE) {
                isThereMinPoint = true;
            }
            allCards.add(createCard(String.valueOf(num)));
            numbers.add(num);
        }
        if (isThereMinPoint) {
            SwingUtilities.invokeLater(relayout);
            return;
        }
        createCards();
    }

    private static JComponent createCard(String text) {
        int number = Integer.parseInt(text);
        JComponent card;
        if (number <= MIN_POINT_VALUE) {
            JButton button = new JButton(text);
            button.setFont(button.getFont().deriveFont(Font.BOLD, 18f));
            button.addActionListener(e -> recalculateNumbers(text));
            button.setHorizontalAlignment(SwingConstants.CENTER);
            card = button;
        } else {
            card = new JPanel(new GridBagLayout());
            JLabel label = new JLabel(text);
            label.setFont(label.getFont().deriveFont(Font.BOLD, 18f));
            card.add(label);
        }
        card.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        card.setBackground(new Color(250, 250, 250));
        card.setPreferredSize(new Dimension(120, 60));
        return card;
    }

    private static void recalculateNumbers(String value) {
        int number = Integer.parseInt(value);
        if (number <= MIN_POINT_VALUE) {
            allCards.clear();
            numbers.clear();
            createCards();
            relayout.run();
        }
    }

    private static void relayoutCards(List<JComponent> components, int viewportHeight, int vgap, int hgap) {
        cardsPanel.removeAll();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.FIRST_LINE_START;
        gbc.insets = new Insets(0, 0, vgap, hgap);
        gbc.weightx = 0;
        gbc.weighty = 0;

        int y = 0, x = 0;
        int currentHeight = 0;

        for (JComponent comp : components) {
            Dimension d = comp.getPreferredSize();
            int extraGap = (y == 0 ? 0 : vgap);
            if (y > 0 && currentHeight + extraGap + d.height > viewportHeight) {
                x++;
                y = 0;
                currentHeight = 0;
            }
            gbc.gridx = x;
            gbc.gridy = y;
            cardsPanel.add(comp, gbc);

            currentHeight += (y == 0 ? d.height : extraGap + d.height);
            y++;
        }
    }

    private static void sortCards(ActionEvent e) {
        List<Integer> numbersToSort = new ArrayList<>(numbers);
        quickSort(numbersToSort, 0, numbersToSort.size() - 1);
        allCards.clear();
        if (sortingType == SortingTypeEnum.ASCENDING) {
            for (int num : numbersToSort) {
                allCards.add(createCard(String.valueOf(num)));
            }
            sortingType = SortingTypeEnum.DESCENDING;
        } else {
            for (int i = numbersToSort.size() - 1; i >= 0; i--) {
                allCards.add(createCard(String.valueOf(numbersToSort.get(i))));
            }
            sortingType = SortingTypeEnum.ASCENDING;
        }
        numbers = numbersToSort;
        SwingUtilities.invokeLater(relayout);

    }

    private static void quickSort(List<Integer> arr, int low, int high) {
        if (low < high) {
            int pivotIndex = partition(arr, low, high);
            quickSort(arr, low, pivotIndex - 1);
            quickSort(arr, pivotIndex + 1, high);
        }
    }

    private static int partition(List<Integer> numbers, int low, int high) {
        int pivot = numbers.get(high);
        int i = low - 1;
        for (int j = low; j < high; j++) {
            if (numbers.get(j) <= pivot) {
                i++;
                Collections.swap(numbers, i, j);
            }
        }
        Collections.swap(numbers, i + 1, high);
        return i + 1;
    }
}
