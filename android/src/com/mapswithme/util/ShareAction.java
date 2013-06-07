
package com.mapswithme.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.Menu;
import android.view.MenuItem;

import com.mapswithme.maps.Framework;
import com.mapswithme.maps.R;
import com.mapswithme.maps.bookmarks.data.MapObject;

import java.util.HashMap;
import java.util.Map;

public abstract class ShareAction
{
  public final static int ID_SMS = 0xfff1;
  public final static int ID_EMAIL = 0xfff2;
  public final static int ID_ANY = 0xffff;

  @SuppressLint("UseSparseArrays")
  public final static Map<Integer, ShareAction> ACTIONS = new HashMap<Integer, ShareAction>();

  /* Actions*/
  private final static SmsShareAction SMS_SHARE = new SmsShareAction();
  private final static EmailShareAction EMAIL_SHARE = new EmailShareAction();
  private final static AnyShareAction ANY_SHARE = new AnyShareAction();
   
  /* Extras*/
  private static final String EXTRA_SMS_BODY = "sms_body";
  
  /* Types*/
  private static final String TYPE_MESSAGE_RFC822 = "message/rfc822";
  private static final String TYPE_TEXT_PLAIN = "text/plain";
  
  /* URIs*/
  private static final String URI_STRING_SMS = "sms:";
  
  protected final int mId;
  protected final int mNameResId;
  protected final Intent mBaseIntent;

  public static SmsShareAction getSmsShare()
  {
    return SMS_SHARE;
  }

  public static EmailShareAction getEmailShare()
  {
    return EMAIL_SHARE;
  }

  public static AnyShareAction getAnyShare()
  {
    return ANY_SHARE;
  }

  protected ShareAction(int id, int nameResId, Intent baseIntent)
  {
    mId = id;
    mNameResId = nameResId;
    mBaseIntent = baseIntent;
  }

  public Intent getIntent()
  {
    return new Intent(mBaseIntent);
  }

  public int getId()
  {
    return mId;
  }

  public int getNameResId()
  {
    return mNameResId;
  }

  @SuppressLint("NewApi")
  public MenuItem addToMenuIfSupported(Context context, Menu menu, boolean showAsAction)
  {
    if (isSupported(context))
    {
      final String name = context.getResources().getString(getNameResId());
      final MenuItem menuItem = menu.add(Menu.NONE, getId(), getId(), name);
      if (Utils.apiEqualOrGreaterThan(11) && showAsAction)
        menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
      return menuItem;
    }
    return null;
  }

  public boolean isSupported(Context context)
  {
    return Utils.isIntentSupported(context, getIntent());
  }

  public void shareWithText(Activity activity, String body, String subject)
  {
    final Intent intent = getIntent();
    intent.putExtra(Intent.EXTRA_TEXT, body)
          .putExtra(Intent.EXTRA_SUBJECT, subject);

    activity.startActivity(intent);
  }
  
  /**
   * 
   * BASE share method 
   * 
   */
  public void shareMapObject(Activity activity, MapObject mapObject) 
  {
    final String ge0Url  = Framework.getGe0Url(mapObject.getLat(), mapObject.getLon(), mapObject.getScale(), mapObject.getName());
    final String httpUrl = Framework.getHttpGe0Url(mapObject.getLat(), mapObject.getLon(), mapObject.getScale(), mapObject.getName());  
    final String address = Framework.getNameAndAddress4Point(mapObject.getLat(), mapObject.getLon());
    
    final String body = activity.getString(R.string.bookmark_share_email, address, ge0Url, httpUrl);
    final String subject = activity.getString(R.string.bookmark_share_email_subject);

    shareWithText(activity, body, subject);
  }

  /**
   * 
   * SMS 
   *
   */
  public static class SmsShareAction extends ShareAction
  {
    protected SmsShareAction()
    {
      super(ID_SMS, R.string.share_by_message, new Intent(Intent.ACTION_VIEW).setData(Uri.parse(URI_STRING_SMS)));
    }

    @Override
    public void shareWithText(Activity activity, String body, String subject)
    {
      final Intent smsIntent = getIntent();
      smsIntent.putExtra(EXTRA_SMS_BODY, body);
      activity.startActivity(smsIntent);
    }
    
    @Override
    public void shareMapObject(Activity activity, MapObject mapObject)
    {
      final String ge0Url  = Framework.getGe0Url(mapObject.getLat(), mapObject.getLon(), mapObject.getScale(), "");
      final String httpUrl = Framework.getHttpGe0Url(mapObject.getLat(), mapObject.getLon(), mapObject.getScale(), "");
      
      final String body = activity.getString(R.string.bookmark_share_sms, ge0Url, httpUrl);

      shareWithText(activity, body, "");
    }
  }

  /**
   * 
   * EMAIL
   * 
   */
  public static class EmailShareAction extends ShareAction
  {
    protected EmailShareAction()
    {
      super(ID_EMAIL, R.string.share_by_email, new Intent(Intent.ACTION_SEND).setType(TYPE_MESSAGE_RFC822));
    }
  }

  /**
   * 
   * ANYTHING
   *
   */
  public static class AnyShareAction extends ShareAction
  {
    protected AnyShareAction()
    {
      super(ID_ANY, R.string.share, new Intent(Intent.ACTION_SEND).setType(TYPE_TEXT_PLAIN));
    }

    @Override
    public void shareWithText(Activity activity, String body, String subject)
    {
      final Intent intent = getIntent();
      intent.putExtra(Intent.EXTRA_TEXT, body)
            .putExtra(Intent.EXTRA_SUBJECT, subject);
      final String header = activity.getString(R.string.share);
      activity.startActivity(Intent.createChooser(intent, header));
    }
  }

  static
  {
    ACTIONS.put(ID_ANY, getAnyShare());
    ACTIONS.put(ID_EMAIL, getEmailShare());
    ACTIONS.put(ID_SMS, getSmsShare());
  }
}
