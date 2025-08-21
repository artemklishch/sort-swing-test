import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import javax.swing.Box;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public class NumbersTest {
    private static final JFrame FRAME = new JFrame("Numbers");
    private static final String START_PAGE = "START_PAGE";
    private static final String SORT_PAGE = "SORT_PAGE";
    private static final ArrayList<JComponent> allCards = new ArrayList<>();
    private static final JPanel CARDS_PANEL = new JPanel(new GridBagLayout());
    private static final CardLayout CARD_LAYOUT = new CardLayout();
    private static final Random RANDOM = new Random();
    private static final int MAX_VALUE = 1000;
    private static final int MIN_POINT_VALUE = 30;
    private static final JTextField NUMBER_FIELD = new JTextField();
    private static final List<Integer> NUMBERS = new ArrayList<>();
    private static final Timer SORT_ANIMATION_TIMER = new Timer(50, null);
    private static final List<int[]> SWAP_INDEXES = new ArrayList<>();
    private static Runnable relayout;
    private static int insertedValue;
    private static JScrollPane scroll;
    private static JPanel cards;
    private static JPanel page1;
    private static JPanel page2;
    private static SortingTypeEnum sortingType = SortingTypeEnum.ASCENDING;
    private static boolean isSorted = false;

    enum SortingTypeEnum {ASCENDING, DESCENDING}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            FRAME.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            cards = new JPanel(CARD_LAYOUT);
            page1 = createStartPage();
            page2 = createSortPage();
            cards.add(page1, START_PAGE);
            cards.add(page2, SORT_PAGE);
            FRAME.setContentPane(cards);
            FRAME.pack();
            FRAME.setLocationRelativeTo(null);
            FRAME.setVisible(true);
        });
    }

    private static JPanel createStartPage() {
        JPanel page1 = new JPanel();
        page1.setLayout(new BoxLayout(page1, BoxLayout.Y_AXIS));
        page1.setBorder(BorderFactory.createEmptyBorder(400, 500, 400, 500));
        JLabel label = new JLabel("How many numbers to display?");
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        NUMBER_FIELD.setColumns(20);
        NUMBER_FIELD.setAlignmentX(Component.CENTER_ALIGNMENT);
        JButton enterButton = new JButton("Enter");
        enterButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        page1.add(label);
        page1.add(Box.createVerticalStrut(8));
        page1.add(NUMBER_FIELD);
        page1.add(Box.createVerticalStrut(8));
        page1.add(enterButton);
        enterButton.addActionListener(NumbersTest::enterButtonHandler);
        return page1;
    }

    private static JPanel createSortPage() {
        JPanel page2 = new JPanel(new BorderLayout(12, 12));
        scroll = new JScrollPane(
                CARDS_PANEL, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED
        );
        scroll.getVerticalScrollBar().setUnitIncrement(24);
        page2.add(scroll, BorderLayout.CENTER);
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
            CARDS_PANEL.revalidate();
            CARDS_PANEL.repaint();
        };
        sortButton.addActionListener(NumbersTest::sortCards);
        resetButton.addActionListener(NumbersTest::resetHandler);
        return page2;
    }

    private static void enterButtonHandler(ActionEvent e) {
        try {
            String trimmedString = NUMBER_FIELD.getText().trim();
            int value = Integer.parseInt(trimmedString);
            if (value <= 0 || value > MAX_VALUE) {
                showErrorModal("Invalid number, has to be over 0 and can't be over " + MAX_VALUE);
                return;
            }
            insertedValue = value;
            createCards();
            CARD_LAYOUT.show(cards, SORT_PAGE);
        } catch (NumberFormatException ex) {
            showErrorModal("Invalid number");
        } finally {
            NUMBER_FIELD.setText("");
        }
    }

    private static void resetStates() {
        allCards.clear();
        NUMBERS.clear();
        isSorted = false;
        SWAP_INDEXES.clear();
        if (sortingType == SortingTypeEnum.DESCENDING) {
            sortingType = SortingTypeEnum.ASCENDING;
        }
    }

    private static void resetHandler(ActionEvent e) {
        resetStates();
        insertedValue = 0;
        CARD_LAYOUT.show(cards, START_PAGE);
    }

    private static void createCards() {
        generateNumbers();
        for (int num : NUMBERS) {
            allCards.add(createCard(String.valueOf(num)));
        }
        relayout.run();
    }

    private static void generateNumbers() {
        NUMBERS.clear();
        boolean isThereMinPoint = false;
        for (int i = 1; i <= insertedValue; i++) {
            int num = RANDOM.nextInt(1, MAX_VALUE);
            if (num <= MIN_POINT_VALUE && !isThereMinPoint) {
                isThereMinPoint = true;
            }
            NUMBERS.add(num);
        }
        if (isThereMinPoint) {
            return;
        }
        generateNumbers();
    }

    private static JButton createCard(String text) {
        JButton button = new JButton(text);
        button.setFont(button.getFont().deriveFont(Font.BOLD, 18f));
        button.setHorizontalAlignment(SwingConstants.CENTER);
        button.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        button.setBackground(new Color(250, 250, 250));
        button.setPreferredSize(new Dimension(120, 60));
        button.addActionListener(e -> recalculateNumbers(text));
        return button;
    }

    private static void recalculateNumbers(String value) {
        int number = Integer.parseInt(value);
        if (number <= MIN_POINT_VALUE) {
            resetStates();
            insertedValue = number;
            createCards();
        } else {
            showErrorModal("Please select a value smaller or equal to " + MIN_POINT_VALUE);
        }
    }

    private static void relayoutCards(List<JComponent> components, int viewportHeight, int vgap, int hgap) {
        CARDS_PANEL.removeAll();
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
            CARDS_PANEL.add(comp, gbc);
            currentHeight += (y == 0 ? d.height : extraGap + d.height);
            y++;
        }
    }

    private static void sortCards(ActionEvent e) {
        if (!isSorted) {
            quickSort(NUMBERS, 0, NUMBERS.size() - 1);
            playSwapsReusableTimer();
            sortingType = SortingTypeEnum.DESCENDING;
            isSorted = true;
        } else {
            reversingSortedCards();
            SwingUtilities.invokeLater(relayout);
        }
    }

    private static void playSwapsReusableTimer() {
        for (var l : SORT_ANIMATION_TIMER.getActionListeners()) {
            SORT_ANIMATION_TIMER.removeActionListener(l);
        }
        AtomicInteger index = new AtomicInteger(0);
        SORT_ANIMATION_TIMER.addActionListener(e -> {
            int i = index.getAndIncrement();
            if (i >= SWAP_INDEXES.size()) {
                SORT_ANIMATION_TIMER.stop();
                return;
            }
            Collections.swap(allCards, SWAP_INDEXES.get(i)[0], SWAP_INDEXES.get(i)[1]);
            relayout.run();
        });
        SORT_ANIMATION_TIMER.restart();
    }

    private static void reversingSortedCards() {
        allCards.clear();
        if (sortingType == SortingTypeEnum.DESCENDING) {
            for (int i = NUMBERS.size() - 1; i >= 0; i--) {
                allCards.add(createCard(String.valueOf(NUMBERS.get(i))));
            }
        } else {
            for (int num : NUMBERS) {
                allCards.add(createCard(String.valueOf(num)));
            }
        }
        sortingType = sortingType == SortingTypeEnum.DESCENDING ? SortingTypeEnum.ASCENDING : SortingTypeEnum.DESCENDING;
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
                SWAP_INDEXES.add(new int[]{i, j});
            }
        }
        Collections.swap(numbers, i + 1, high);
        SWAP_INDEXES.add(new int[]{i + 1, high});
        return i + 1;
    }

    private static void showErrorModal(String errorMessage) {
        JOptionPane.showMessageDialog(FRAME, errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
