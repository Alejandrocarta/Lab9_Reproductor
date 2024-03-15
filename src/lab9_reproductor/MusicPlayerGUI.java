/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lab9_reproductor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;

/**
 *
 * @author aleja
 */
public class MusicPlayerGUI extends JFrame {
    private MusicPlayer player;
    private JButton playButton;
    private JButton stopButton;
    private JButton pauseButton;
    private JButton removeButton;
    private JButton addButton;
    private JButton selectButton;
    private JButton nextButton;
    private JButton previousButton;
    private JList<String> playlist;
    private DefaultListModel<String> listModel;
    private JProgressBar progressBar;
    private JLabel songLabel;
    private Timer timer;
    private JLabel durationLabel;
    private JLabel currentPositionLabel;

    public MusicPlayerGUI() {
        player = new MusicPlayer();
        listModel = new DefaultListModel<>();
        playlist = new JList<>(listModel);
        

        removeButton = new JButton("Delete");
        playButton = new JButton("Play");
        stopButton = new JButton("Stop");
        pauseButton = new JButton("Pause");
        addButton = new JButton("Add");
        selectButton = new JButton("Select");
        nextButton = new JButton("Next");
        

        playButton.addActionListener(e -> {
            int selectedIndex = playlist.getSelectedIndex();
            if (selectedIndex != -1) {
                player.selectSong(selectedIndex);
                player.play();
                updateProgressBar();
                int index = playlist.getSelectedIndex();
                 songLabel.setText("Escuchando: " + listModel.getElementAt(index));
            } else {
                JOptionPane.showMessageDialog(this, "No hay canción seleccionada.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        stopButton.addActionListener(e -> {
            player.stop();
            progressBar.setValue(0);
            if (timer.isRunning()) {
                timer.stop();
                int index = playlist.getSelectedIndex();
                 songLabel.setText("Seleccionada: " + listModel.getElementAt(index));
                playButton.setEnabled(true);
                pauseButton.setText("Pause");
            }
        });
        
        removeButton.addActionListener(new ActionListener(){
           @Override
           public void actionPerformed(ActionEvent e ){
               int SelectedIndex = playlist.getSelectedIndex();
               if (SelectedIndex != -1) {
                   listModel.remove(SelectedIndex);
               }
           }
        });

        pauseButton.addActionListener(e -> {
            if (player.isPlaying()) {
                player.pause();
                pauseButton.setText("Resume");
                playButton.setEnabled(false);
            } else {
                player.resume();
                pauseButton.setText("Pause");
                playButton.setEnabled(true);
            }
        });

        addButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fileChooser.setFileFilter(new FileNameExtensionFilter("WAV files", "wav"));
            int returnValue = fileChooser.showOpenDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                File musicFolder = new File("src/Music");
                if (!musicFolder.exists()) {
                    musicFolder.mkdirs();
                }
                File newFile = new File(musicFolder, selectedFile.getName());
                try {
                    Files.copy(selectedFile.toPath(), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    listModel.addElement(selectedFile.getName().replace(".wav", ""));
                    player.addSong(newFile.getAbsolutePath());
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        
        nextButton.addActionListener(e -> player.nextSong());
        
        
        progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        progressBar.setForeground(Color.CYAN);

        songLabel = new JLabel("Empezar a escuchar.");

        playlist.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int index = playlist.getSelectedIndex();
                if (index != -1) {
                    player.selectSong(index);
                    songLabel.setText("Seleccionada: " + listModel.getElementAt(index));
                    progressBar.setValue(0);
                }
            }
        });

        timer = new Timer(100, e -> {
            if (player.isPlaying()) {
                long currentSeconds = player.getCurrentMicroseconds() / 1_000_000;
                long totalSeconds = player.getTotalSeconds();
                int progress = (int) ((double) currentSeconds / totalSeconds * 100);
                progressBar.setValue(progress);

                long minutes = totalSeconds / 60;
                long seconds = totalSeconds % 60;
                durationLabel.setText(String.format("%02d:%02d", minutes, seconds));

                long currentMinutes = currentSeconds / 60;
                long currentSecondsRemaining = currentSeconds % 60;
                currentPositionLabel.setText(String.format("%02d:%02d", currentMinutes, currentSecondsRemaining));
            }
        });

        loadSongsFromDefaultFolder();

        JPanel controlPanel = new JPanel();
        controlPanel.add(playButton);
        controlPanel.add(stopButton);
        controlPanel.add(pauseButton);
        controlPanel.add(addButton);
        controlPanel.add(selectButton);
        controlPanel.add(nextButton);
        controlPanel.add(removeButton);

        JPanel progressPanel = new JPanel();
        progressPanel.setLayout(new BorderLayout());
        progressPanel.add(progressBar, BorderLayout.CENTER);

        durationLabel = new JLabel("00:00");
        durationLabel.setHorizontalAlignment(SwingConstants.LEFT);
        progressPanel.add(durationLabel, BorderLayout.EAST);

        currentPositionLabel = new JLabel("00:00");
        currentPositionLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        progressPanel.add(currentPositionLabel, BorderLayout.WEST);

        progressPanel.add(songLabel, BorderLayout.NORTH);
        progressBar.setStringPainted(false);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(new JScrollPane(playlist), BorderLayout.CENTER);
        getContentPane().add(controlPanel, BorderLayout.SOUTH);
        getContentPane().add(progressPanel, BorderLayout.NORTH);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600); 
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void updateProgressBar() {
        if (!timer.isRunning()) {
            timer.start();
        }
    }

    private void loadSongsFromDefaultFolder() {
        File musicFolder = new File("src/Music");
        if (musicFolder.exists() && musicFolder.isDirectory()) {
            File[] files = musicFolder.listFiles((dir, name) -> name.toLowerCase().endsWith(".wav"));
            if (files != null) {
                for (File file : files) {
                    listModel.addElement(file.getName().replace(".wav", ""));
                    player.addSong(file.getAbsolutePath());
                }
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MusicPlayerGUI::new);
    }
}