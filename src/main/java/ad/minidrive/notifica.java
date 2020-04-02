/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ad.minidrive;

/**
 *
 * @author Hemihundias
 */
public class notifica extends Thread
{
    private Connection conn;

    public notifica(Connection conn)
    {
        this.conn = conn;
    }

    public void run()
    {
        while (true)
        {
            try
            {
                Statement stmt = conn.createStatement();
                stmt.execute("NOTIFY mymessage");
                stmt.close();
                Thread.sleep(2000);
            }
            catch (SQLException sqle)
            {
                sqle.printStackTrace();
            }
            catch (InterruptedException ie)
            {
                ie.printStackTrace();
            }
        }
    }

}
