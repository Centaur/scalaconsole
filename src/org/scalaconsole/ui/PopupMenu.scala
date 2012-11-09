package org.scalaconsole.ui

/**
 * Created by IntelliJ IDEA.
 * User: xf
 * Date: 11-12-9
 * Time: 上午10:14
 */

import javax.swing.JPopupMenu
import scala.swing.{ Component, MenuItem }
import scala.swing.SequentialContainer.Wrapper

object PopupMenu {
  private[PopupMenu] trait JPopupMenuMixin { def popupMenuWrapper: PopupMenu }
}

class PopupMenu extends Component with Wrapper {

  override lazy val peer: JPopupMenu = new JPopupMenu with PopupMenu.JPopupMenuMixin with SuperMixin {
    def popupMenuWrapper = PopupMenu.this
  }

  def show(invoker: Component, x: Int, y: Int) {
    peer.show(invoker.peer, x, y)
  }

  /* Create any other peer methods here */
}