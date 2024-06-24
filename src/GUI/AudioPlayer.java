package GUI;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class AudioPlayer {

    private Clip audioClip;


    public void playAudio(String audioFilePath) {
        try {
            File audioFile = new File(audioFilePath);
            if (!audioFile.exists()) {
                System.out.println("Audio file not found: " + audioFilePath);
                return;
            }

            AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
            audioClip = AudioSystem.getClip();
            audioClip.open(audioStream);
            audioClip.start();
        } catch (UnsupportedAudioFileException e) {
            System.out.println("The specified audio file is not supported.");
            e.printStackTrace();
        } catch (LineUnavailableException e) {
            System.out.println("Audio line for playing back is unavailable.");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Error playing the audio file.");
            e.printStackTrace();
        }
    }

    public void stopAudio() {
        if (audioClip != null && audioClip.isRunning()) {
            audioClip.stop();
        }
    }

    public void closeAudio() {
        if (audioClip != null) {
            audioClip.close();
        }
    }

    public void playSelectPieceSound() {
        String selectPieceSound = "src/res/sounds/selectPieceSound1.wav";
        playAudio(selectPieceSound);
    }

    public void playMovingPieceSound() {
        String movingPieceSound = "src/res/sounds/moveSound1.wav";
        playAudio(movingPieceSound);
    }

    public void playCheckSound() {
        String checkSound = "src/res/sounds/checkSound1.wav";
        playAudio(checkSound);
    }

    public void playInvalidMoveSound() {
        String invalidMoveSound = "src/res/sounds/invalidMoveSound1.wav";
        playAudio(invalidMoveSound);
    }

    public void playInvalidMoveBecauseOfCheckSound() {
        String invalidMoveBecauseOfCheckSound = "src/res/sounds/invalidMove2.wav";
        playAudio(invalidMoveBecauseOfCheckSound);
    }

    public void playCheckMateSound() {
        String checkMateSound = "src/res/sounds/winningSound1.wav";
        playAudio(checkMateSound);
    }

    public void playDrawSound() {
        String drawSound = "src/res/sounds/drawSound1.wav";
        playAudio(drawSound);
    }

    public void playCastlingSound() {
        String castlingSound = "src/res/sounds/castlingSound1.wav";
        playAudio(castlingSound);
    }


}

