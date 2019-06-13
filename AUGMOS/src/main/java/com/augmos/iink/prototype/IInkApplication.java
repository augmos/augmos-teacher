// Copyright MyScript. All rights reserved.

package com.augmos.iink.prototype;

import android.app.Application;

import com.augmos.certificate.MyCertificate;
import com.myscript.iink.Engine;

public class IInkApplication extends Application
{
  private static Engine engine;

  public static synchronized Engine getEngine()
  {
    if (engine == null)
    {
      engine = Engine.create(MyCertificate.getBytes());
    }

    return engine;
  }

}
