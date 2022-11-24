package com.hlju.Tetris;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
/**テトリスを表示するフレームおよび起動するメインメソッドが含まれたクラス*/
public class TetrisApp extends JFrame {
	//どこからも参照されません。
	//gitted from https://github.com/HelloClyde/Tetris-Swing
	private static final long serialVersionUID = 8995729671326316569L;
	/**メインボード*/
	Tetris[] tetris = new Tetris[21];
	/**コンストラクタ<br />
	 * 各種コンポーネントの初期化および登録。
	 * */
	public TetrisApp() {
		this.setUndecorated(true);
		this.setLayout(null);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setBounds(0,0,1920,1080);
		this.setTitle("Tetris Remake");
		this.setResizable(false);
		JMenuBar menu = new JMenuBar();
		this.setJMenuBar(menu);
		JMenu gameMenu = new JMenu("メニュー");
		JMenuItem newGameItem = gameMenu.add("ニューゲーム");
		newGameItem.addActionListener(this.NewGameAction);
		JMenuItem pauseItem = gameMenu.add("中断");
		pauseItem.addActionListener(this.PauseAction);
		JMenuItem continueItem = gameMenu.add("再開");
		continueItem.addActionListener(this.ContinueAction);
		JMenuItem exitItem = gameMenu.add("終了");
		exitItem.addActionListener(this.ExitAction);
		JMenu modeMenu = new JMenu("モード");
		JMenuItem v4Item = modeMenu.add("v4");
		v4Item.addActionListener(this.v4Action);
		JMenuItem v6Item = modeMenu.add("v6");
		v6Item.addActionListener(this.v6Action);
		JMenu helpMenu = new JMenu("about");
		JMenuItem aboutItem = helpMenu.add("aboutの表示");
		aboutItem.addActionListener(this.AboutAction);
		
		menu.add(gameMenu);
		menu.add(modeMenu);
		menu.add(helpMenu);
		for(int i=0;i<tetris.length;i++) {
			Tetris _tet=new Tetris(i);
			tetris[i]=_tet;
			tetris[i].setBounds((i%7)*274,(i/7)*350,274,350);
			this.add(tetris[i]);
			this.tetris[i].setFocusable(true);
		}

	}



	/**
	 *  JFrameを継承した自身をインスタンス化！
	 * @param
	 */
	static public void main(String... args) {
		TetrisApp tetrisApp = new TetrisApp();
		tetrisApp.setVisible(true);
	}

	/**
	 * メニューのニューゲームが押された時実行されるリスナー*/
	ActionListener NewGameAction = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO  Զ    ɵķ      
			for(int i=0;i<tetris.length;i++) {
				TetrisApp.this.tetris[i].Initial();
			}
		}
	};
	/**メニューの中断ボタンが押された時実行されるリスナー*/
	ActionListener PauseAction = new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO  Զ    ɵķ      
			for(int i=0;i<tetris.length;i++) {
				TetrisApp.this.tetris[i].SetPause(true);
			}
		}
	};
	/**メニューの再開ボタンが押された時実行されるリスナー*/
	ActionListener ContinueAction = new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO  Զ    ɵķ      
			for(int i=0;i<tetris.length;i++) {
				TetrisApp.this.tetris[i].SetPause(false);
			}
		}
	};
	/**メニューの終了ボタンが押された時実行されるリスナー*/
	ActionListener ExitAction = new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO  Զ    ɵķ      

			System.exit(0);
		}
	};
	/**aboutのaboutボタンが押された時実行されるリスナー*/
	ActionListener AboutAction = new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
			JOptionPane.showMessageDialog(TetrisApp.this, "Tetris Remake Ver 1.0", "    ", JOptionPane.WARNING_MESSAGE);
		}
	};
	/**modeのv4ボタンが押された時実行されるリスナー*/
	ActionListener v4Action = new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO  Զ    ɵķ      
			for(int i=0;i<tetris.length;i++) {
				TetrisApp.this.tetris[i].SetMode("v4");
			}
		}
	};
	/**modeのv6ボタンが押された時実行されるリスナー*/
	ActionListener v6Action = new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO  Զ    ɵķ      
			for(int i=0;i<tetris.length;i++) {
				TetrisApp.this.tetris[i].SetMode("v6");
			}
		}
	};
}
