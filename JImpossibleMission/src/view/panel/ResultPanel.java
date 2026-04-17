package view.panel;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.swing.JLabel;
import javax.swing.SwingConstants;

import model.Game;
import model.Leaderboard;
import model.Play.PlayState;
import model.RecordSave;
import model.ScoreManager;
import view.Navigator;
import view.util.*;

/**
 * Panel used to show the result of the Play match
 */
public class ResultPanel extends Panel {

	/**
	 * Default Serial UID
	 */
	private static final long serialVersionUID = 1L;
	
	// Use for the leaderboard
	private static final String DEFAULT_ID = "-----";
	private static final String DEFAULT_POINTS = "0";
	
	// Positions to adjust with the result background
	private static final int XBORDER_LEFT = 48;
	private static final int XBORDER_RIGHT = 912;
	private static final int WSPACE_WHOLE = 864;
	private static final int WSPACE_HALF = WSPACE_WHOLE / 2;
	private static final int HSPACE = 21;
	private static final int XCALCUL = 480;
	private static final int XPOINTS = 768;
	
	// Default font size
	private static final int DEFAULT_FONT_SIZE = 16;
	
	// Components
	private BufferedImage resultBg;
	private JLabel pieces, piePoints, pieCalc, pass, pasCalc, pasPoints, puzzles, puzCalc, puzPoints,
			secRemaining, secPoints, failed, accomplished, addPoints, total, totPoints, prevScore, prevPoints,
			mOrNg, hallOfFame;
	private List<JLabel> ids, points;

	/**
	 * ResultPanel constructor that initialize and add the components
	 * 
	 * @param n  Navigator used to give the command to change panel
	 */
	public ResultPanel(Navigator n) {
		super(n, null);
		initComponents();
		addComponents();
	}

	/**
	 * Initialize the components
	 */
	@Override
	protected void initComponents() {
		// Background
		resultBg = LoadImage.getImage("result.png");
		
		// Puzzle pieces found + Calculation + Points text
		pieces = FactoryComponents.createLabel("PUZZLE PIECES FOUND", DEFAULT_FONT_SIZE, FactoryComponents.YELLOW_CHARACTER, SwingConstants.LEFT);
		pieces.setBounds(XBORDER_LEFT, 24, WSPACE_HALF, HSPACE);
		pieCalc = FactoryComponents.createLabel(getCalcText(0, 0), DEFAULT_FONT_SIZE, Color.WHITE, SwingConstants.RIGHT);
		pieCalc.setBounds(XCALCUL, 24, XPOINTS - XCALCUL + 90, HSPACE);
		piePoints = FactoryComponents.createLabel(getPointsText(0), DEFAULT_FONT_SIZE, Color.WHITE, SwingConstants.RIGHT);
		piePoints.setBounds(XPOINTS, 24, XBORDER_RIGHT - XPOINTS, HSPACE);
		
		// Passes found + Calculation + Points text
		pass = FactoryComponents.createLabel("PASSWORD FOUND", DEFAULT_FONT_SIZE, FactoryComponents.YELLOW_CHARACTER, SwingConstants.LEFT);
		pass.setBounds(XBORDER_LEFT, 48, WSPACE_HALF, HSPACE);
		pasCalc = FactoryComponents.createLabel(getCalcText(0, 0), DEFAULT_FONT_SIZE, Color.WHITE, SwingConstants.RIGHT);
		pasCalc.setBounds(XCALCUL, 48, XPOINTS - XCALCUL + 90, HSPACE);
		pasPoints = FactoryComponents.createLabel(getPointsText(0), DEFAULT_FONT_SIZE, Color.WHITE, SwingConstants.RIGHT);
		pasPoints.setBounds(XPOINTS, 48, XBORDER_RIGHT - XPOINTS, HSPACE);
		
		// Puzzle solved + Calculation + Points text
		puzzles = FactoryComponents.createLabel("PUZZLES SOLVED", DEFAULT_FONT_SIZE, FactoryComponents.YELLOW_CHARACTER, SwingConstants.LEFT);
		puzzles.setBounds(XBORDER_LEFT, 120, WSPACE_HALF, HSPACE);
		puzCalc = FactoryComponents.createLabel(getCalcText(0, 0), DEFAULT_FONT_SIZE, Color.WHITE, SwingConstants.RIGHT);
		puzCalc.setBounds(XCALCUL, 120, XPOINTS - XCALCUL + 90, HSPACE);
		puzPoints = FactoryComponents.createLabel(getPointsText(0), DEFAULT_FONT_SIZE, Color.WHITE, SwingConstants.RIGHT);
		puzPoints.setBounds(XPOINTS, 120, XBORDER_RIGHT - XPOINTS, HSPACE);
		
		// Seconds remaining + Points
		secRemaining = FactoryComponents.createLabel("SECONDS REMAINING", DEFAULT_FONT_SIZE, FactoryComponents.YELLOW_CHARACTER, SwingConstants.LEFT);
		secRemaining.setBounds(XBORDER_LEFT, 144, WSPACE_HALF, 21);
		secPoints = FactoryComponents.createLabel(getPointsText(0), DEFAULT_FONT_SIZE, Color.WHITE, SwingConstants.RIGHT);
		secPoints.setBounds(XPOINTS, 144, XBORDER_RIGHT - XPOINTS, HSPACE);

		// Text when LOST
		failed = FactoryComponents.createLabel("M  I  S  S  I  O  N     T  E  R  M  I  N  A  T  E  D", DEFAULT_FONT_SIZE, FactoryComponents.YELLOW_CHARACTER);
		failed.setBounds(XBORDER_LEFT, 216, WSPACE_WHOLE, HSPACE);

		// Text when WIN + Points text
		accomplished = FactoryComponents.createLabel("MISSION COMPLETE", DEFAULT_FONT_SIZE, FactoryComponents.YELLOW_CHARACTER, SwingConstants.LEFT);
		accomplished.setBounds(XBORDER_LEFT, 216, WSPACE_HALF, HSPACE);
		addPoints = FactoryComponents.createLabel("1000", DEFAULT_FONT_SIZE, Color.WHITE, SwingConstants.RIGHT);
		addPoints.setBounds(XPOINTS, 216, XBORDER_RIGHT - XPOINTS, HSPACE);

		// Total score + Points text
		total = FactoryComponents.createLabel("T O T A L   S C O R E", DEFAULT_FONT_SIZE, FactoryComponents.YELLOW_CHARACTER, SwingConstants.LEFT);
		total.setBounds(XBORDER_LEFT, 288, WSPACE_HALF, HSPACE);
		totPoints = FactoryComponents.createLabel(getPointsText(0), DEFAULT_FONT_SIZE, Color.WHITE, SwingConstants.RIGHT);
		totPoints.setBounds(XPOINTS, 288, XBORDER_RIGHT - XPOINTS, HSPACE);

		// Surpassing the previous highscore
		prevScore = FactoryComponents.createLabel("THIS SURPASSES THE PREVIOUS HIGH SCORE OF", 16, FactoryComponents.GREEN_CHARACTER, SwingConstants.LEFT);
		prevScore.setBounds(XBORDER_LEFT, 312, WSPACE_WHOLE, HSPACE);
		prevPoints = FactoryComponents.createLabel(getPointsText(0), DEFAULT_FONT_SIZE, Color.WHITE, SwingConstants.RIGHT);
		prevPoints.setBounds(XPOINTS, 312, XBORDER_RIGHT - XPOINTS, HSPACE);

		// Menu or New Game
		mOrNg = FactoryComponents.createLabel("HIT 'ESC' TO GET BACK TO THE MENU OR 'R' TO START A NEW GAME", DEFAULT_FONT_SIZE, FactoryComponents.YELLOW_CHARACTER);
		mOrNg.setBounds(XBORDER_LEFT, 408, WSPACE_WHOLE, HSPACE);

		// Hall of Fame + Leaderboard
		hallOfFame = FactoryComponents.createLabel("HALL OF FAME", DEFAULT_FONT_SIZE, FactoryComponents.YELLOW_CHARACTER);
		hallOfFame.setBounds(XBORDER_LEFT, 432, WSPACE_WHOLE, HSPACE);
		
		ids = new ArrayList<JLabel>();
		points = new ArrayList<JLabel>();
		for (int i=0; i<Leaderboard.MAX_CAPACITY; i++) {
			ids.add(FactoryComponents.createLabel(DEFAULT_ID, DEFAULT_FONT_SIZE, Color.WHITE, SwingConstants.LEFT));
			points.add(FactoryComponents.createLabel(getPointsText(0), 16, Color.WHITE, SwingConstants.RIGHT));
		}
	}

	/**
	 * Add the components in their position
	 */
	@Override
	protected void addComponents() {
		
		add(pieces);
		add(pieCalc);
		add(piePoints);

		add(pass);
		add(pasCalc);
		add(pasPoints);

		add(puzzles);
		add(puzCalc);
		add(puzPoints);

		add(secRemaining);
		add(secPoints);

		add(accomplished);
		add(addPoints);
		add(failed);
		accomplished.setVisible(false);
		addPoints.setVisible(false);
		failed.setVisible(false);
		
		add(total);
		add(totPoints);

		add(prevScore);
		add(prevPoints);
		prevScore.setVisible(false);
		prevPoints.setVisible(false);

		add(mOrNg);

		add(hallOfFame);
		
		for (int i=0; i<Leaderboard.MAX_CAPACITY; i++) {
			JLabel id = ids.get(i);
			JLabel point = points.get(i);
			id.setBounds(XBORDER_LEFT + 132 + ((i/5) * 288), 456 + ((i%5) * 24), 120, HSPACE);
			point.setBounds(XBORDER_LEFT + ((i/5) * 288), 456 + ((i%5) * 24), 120, HSPACE);
			add(id);
			add(point);
		}
	}

	/**
	 * Paints a different background for RESULT
	 */
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.drawImage(resultBg, 0, 0, GAME_WIDTH, GAME_HEIGHT, null);
	}
	
	/**
	 * Text for the calculation
	 * 
	 * @param amount  The amount of times you have to get the points
	 * @param times   Default amount of points that gets times with
	 * 
	 * @return A String that represents the calculation of int amount times int times
	 * Example. amount = 4, times = 100 -> "4 x 100"
	 */
	private String getCalcText(int amount, int times) {
		return amount + " x "+ times +"  =  ";
	}
	
	/**
	 * Text for the points
	 * 
	 * @param points  Amount of points
	 * 
	 * @return Simply Return the number int to String
	 */
	private String getPointsText(int points) {
		return Integer.toString(points);
	}
	
	/**
	 * Updates the text to represents the passes and points that the player got in the game + leaderboard
	 * 
	 * @param g       Used to get the points and the leaderboard
	 * @param result  Which PlayState end with the Play
	 */
	public void updateResult(Game g, PlayState result) {
		if (Optional.ofNullable(g.getPlay()).isEmpty())
			return;
		
		ScoreManager scores = g.getPlay().getScores();
		// Puzzle Pieces
		pieCalc.setText(getCalcText(scores.getPuzzlePiecesFound(), ScoreManager.PIECE_POINTS));
		piePoints.setText(getPointsText(scores.getPuzzlePiecesPoints()));
		// Passes (Lift and Robot)
		pasCalc.setText(getCalcText((scores.getLiftPassFound() + scores.getRobotPassFound()), ScoreManager.PIECE_POINTS));
		pasPoints.setText(getPointsText(scores.getPassPoints()));
		// Completed puzzles
		puzCalc.setText(getCalcText(scores.getCompletedPuzzles(), ScoreManager.PUZZLE_POINTS));
		puzPoints.setText(getPointsText(scores.getPuzzlePoints()));
		// Seconds remaining
		secPoints.setText(getPointsText(scores.getSecPoints()));
		// Sets the victory/loser text
		switch(result) {
		case WIN -> {
			accomplished.setVisible(true);
			addPoints.setVisible(true);
		}
		case LOST -> failed.setVisible(true);
		default -> {}
		}
		// Total points
		totPoints.setText(getPointsText(scores.getTotalPoints()));
		// In case of highscore shows the Highscore text
		if(g.getLeaderboard().hasNewHighScore()) {
			prevScore.setVisible(true);
			if (g.getLeaderboardList().size() >= 2)
				prevPoints.setText(getPointsText(g.getLeaderboardList().get(1).getPoints()));
			prevPoints.setVisible(true);
		}
		// Hall of Fame (Leaderboard)
		int index = 0;
		for (RecordSave r : g.getLeaderboardList()) {
			ids.get(index).setText(r.getID());
			points.get(index).setText(getPointsText(r.getPoints()));
			index++;
		}
		
		repaint();
	}

	/**
	 * Resets all the text and disable the optional ones (victory/loser and highscore text)
	 */
	public void reset() {
		accomplished.setVisible(false);
		addPoints.setVisible(false);
		failed.setVisible(false);
		prevScore.setVisible(false);
		prevPoints.setText(DEFAULT_POINTS);
		prevPoints.setVisible(false);
		ids.stream()
		.filter(r -> r.getText() != DEFAULT_ID)
		.forEach(r -> r.setText(DEFAULT_ID));
		points.stream()
		.filter(r -> r.getText() != DEFAULT_POINTS)
		.forEach(r -> r.setText(DEFAULT_POINTS));
	}

}
